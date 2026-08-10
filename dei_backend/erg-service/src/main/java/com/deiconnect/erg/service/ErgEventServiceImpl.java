package com.deiconnect.erg.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.erg.dto.CreateEventRequest;
import com.deiconnect.erg.dto.EventResponse;
import com.deiconnect.erg.dto.UpdateEventRequest;
import com.deiconnect.erg.dto.EventParticipationResponse;
import com.deiconnect.erg.dto.ParticipantResponse;
import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.entity.ERGEvent;
import com.deiconnect.erg.entity.ERGEventParticipation;
import com.deiconnect.erg.enums.EventStatus;
import com.deiconnect.erg.enums.MembershipStatus;
import com.deiconnect.erg.mapper.ErgEventMapper;
import com.deiconnect.erg.repository.ErgEventRepository;
import com.deiconnect.erg.repository.ErgMembershipRepository;
import com.deiconnect.erg.repository.ERGEventParticipationRepository;
import com.deiconnect.erg.client.UserClient;
import com.deiconnect.erg.client.UserResponse;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ErgEventServiceImpl implements ErgEventService {

    private final ErgEventRepository eventRepository;
    private final ErgService ergService;
    private final ErgEventMapper eventMapper;
    private final AuditLogWriter auditLogWriter;
    private final ErgMembershipRepository membershipRepository;
    private final UserClient userClient;
    private final ERGEventParticipationRepository participationRepository;

    @Override
    @Transactional
    public EventResponse create(Long ergId, CreateEventRequest request) {
        ERG erg = ergService.requireManageableChapter(ergId);
        ERGEvent event = ERGEvent.builder()
                .erg(erg)
                .eventName(request.eventName())
                .eventType(request.eventType())
                .date(request.date())
                .attendeeCount(request.attendeeCount())
                .budgetSpent(request.budgetSpent())
                .status(EventStatus.PLANNED)
                .build();
        event = eventRepository.save(event);
        auditLogWriter.record("CREATE_EVENT", "ERGEvent", event.getId());
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse update(Long ergId, Long eventId, UpdateEventRequest request) {
        ergService.requireManageableChapter(ergId);
        ERGEvent event = loadInErg(ergId, eventId);
        event.setEventName(request.eventName());
        event.setEventType(request.eventType());
        event.setDate(request.date());
        event.setAttendeeCount(request.attendeeCount());
        event.setBudgetSpent(request.budgetSpent());
        event.setStatus(request.status());
        event = eventRepository.save(event);
        auditLogWriter.record("UPDATE_EVENT", "ERGEvent", event.getId());
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public void delete(Long ergId, Long eventId) {
        ergService.requireManageableChapter(ergId);
        ERGEvent event = loadInErg(ergId, eventId);
        eventRepository.delete(event);
        auditLogWriter.record("DELETE_EVENT", "ERGEvent", eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> list(Long ergId, Pageable pageable) {
        SecurityUtils.requireCurrentPrincipal();
        ergService.findOrThrow(ergId);
        return eventRepository.findByErg_Id(ergId, pageable).map(eventMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getById(Long ergId, Long eventId) {
        SecurityUtils.requireCurrentPrincipal();
        return eventMapper.toResponse(loadInErg(ergId, eventId));
    }

    @Override
    @Transactional
    public EventParticipationResponse participate(Long ergId, Long eventId) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        ERGEvent event = loadInErg(ergId, eventId);

        boolean isMember = membershipRepository.findByErg_IdAndEmployeeUserId(ergId, principal.getId())
                .map(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .orElse(false);
        if (!isMember) {
            throw new ForbiddenOperationException("Only active members of the ERG Group are allowed to participate in its events");
        }

        if (participationRepository.existsByEvent_IdAndEmployeeUserId(eventId, principal.getId())) {
            throw new ConflictException("You have already registered for this event");
        }

        UserResponse employee = userClient.getByIdInternal(principal.getId());

        ERGEventParticipation participation = ERGEventParticipation.builder()
                .event(event)
                .employeeUserId(employee.userId())
                .registrationDate(LocalDate.now())
                .build();
        participation = participationRepository.save(participation);

        if (event.getAttendeeCount() == null) {
            event.setAttendeeCount(1);
        } else {
            event.setAttendeeCount(event.getAttendeeCount() + 1);
        }
        eventRepository.save(event);

        auditLogWriter.record(principal.getId(), "REGISTER_EVENT", "ERGEventParticipation", participation.getId());

        return new EventParticipationResponse(
                participation.getId(),
                event.getId(),
                event.getEventName(),
                employee.userId(),
                employee.name(),
                participation.getRegistrationDate()
        );
    }

    @Override
    @Transactional
    public void cancelParticipation(Long ergId, Long eventId) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        ERGEvent event = loadInErg(ergId, eventId);

        ERGEventParticipation participation = participationRepository.findByEvent_IdAndEmployeeUserId(eventId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event participation registration not found"));

        participationRepository.delete(participation);

        if (event.getAttendeeCount() != null && event.getAttendeeCount() > 0) {
            event.setAttendeeCount(event.getAttendeeCount() - 1);
            eventRepository.save(event);
        }

        auditLogWriter.record(principal.getId(), "CANCEL_EVENT_REGISTRATION", "ERGEventParticipation", participation.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> listParticipants(Long ergId, Long eventId) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        ERGEvent event = loadInErg(ergId, eventId);
        ERG erg = event.getErg();

        boolean allowed = false;
        if (principal.getRole() == Role.ADMIN) {
            allowed = true;
        } else if (principal.getRole() == Role.DEI_MANAGER) {
            if (erg.getCreatorManagerId() != null && erg.getCreatorManagerId().equals(principal.getId())) {
                allowed = true;
            }
        } else if (principal.getRole() == Role.ERG_LEAD) {
            if (erg.getErgLeadId() != null && erg.getErgLeadId().equals(principal.getId())) {
                allowed = true;
            }
        }

        if (!allowed) {
            throw new ForbiddenOperationException("You are not permitted to view the participant list for this event");
        }

        List<ERGEventParticipation> participations = participationRepository.findByEvent_Id(eventId);
        if (participations.isEmpty()) {
            return List.of();
        }

        List<Long> employeeUserIds = participations.stream()
                .map(ERGEventParticipation::getEmployeeUserId)
                .toList();
        List<UserResponse> users = userClient.getByIdsInternal(employeeUserIds);
        Map<Long, UserResponse> userMap = new HashMap<>();
        for (UserResponse u : users) {
            userMap.put(u.userId(), u);
        }

        return participations.stream()
                .map(p -> {
                    UserResponse u = userMap.get(p.getEmployeeUserId());
                    String name = u != null ? u.name() : "System User (Offline)";
                    String email = u != null ? u.email() : "";
                    return new ParticipantResponse(
                            p.getEmployeeUserId(),
                            name,
                            email,
                            p.getRegistrationDate()
                    );
                })
                .toList();
    }

    private ERGEvent loadInErg(Long ergId, Long eventId) {
        ERGEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("ERGEvent", eventId));
        if (!event.getErg().getId().equals(ergId)) {
            throw new ForbiddenOperationException("Event does not belong to ERG " + ergId);
        }
        return event;
    }
}
