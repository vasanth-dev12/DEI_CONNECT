package com.deiconnect.erg.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.erg.dto.CreateErgRequest;
import com.deiconnect.erg.dto.ErgResponse;
import com.deiconnect.erg.dto.UpdateErgRequest;
import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;
import com.deiconnect.erg.enums.MembershipStatus;
import com.deiconnect.erg.mapper.ErgMapper;
import com.deiconnect.erg.repository.ErgMembershipRepository;
import com.deiconnect.erg.repository.ErgRepository;
import com.deiconnect.erg.client.UserClient;
import com.deiconnect.erg.client.UserResponse;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ErgServiceImpl implements ErgService {

    private final ErgRepository ergRepository;
    private final ErgMembershipRepository membershipRepository;
    private final UserClient userClient;
    private final ErgMapper ergMapper;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public ErgResponse create(CreateErgRequest request) {
        UserResponse lead = userClient.getByIdInternal(request.ergLeadId());
        if (lead.role() != Role.ERG_LEAD) {
            throw new ConflictException("The Group Head must be a user with the ERG Lead role");
        }

        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        Long creatorManagerId = null;
        if (principal.getRole() == Role.DEI_MANAGER) {
            creatorManagerId = principal.getId();
        }

        if (request.executiveSponsorId() != null) {
            userClient.getByIdInternal(request.executiveSponsorId());
        }

        ERG erg = ERG.builder()
                .ergName(request.ergName())
                .focus(request.focus())
                .mission(request.mission())
                .ergLeadId(request.ergLeadId())
                .executiveSponsorId(request.executiveSponsorId())
                .creatorManagerId(creatorManagerId)
                .memberCount(0)
                .foundedDate(request.foundedDate())
                .status(ErgStatus.ACTIVE)
                .build();
        erg = ergRepository.save(erg);
        auditLogWriter.record("CREATE_ERG", "ERG", erg.getId());
        return ergMapper.toResponse(erg);
    }

    @Override
    @Transactional
    public ErgResponse update(Long id, UpdateErgRequest request) {
        ERG erg = findOrThrow(id);
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() == Role.DEI_MANAGER) {
            if (erg.getCreatorManagerId() == null || !erg.getCreatorManagerId().equals(principal.getId())) {
                throw new ForbiddenOperationException("You may only edit ERG groups you created");
            }
        }

        boolean leadScoped = principal.getRole() == Role.ERG_LEAD;
        if (leadScoped) {
            ensureOwnChapter(erg);
        }

        erg.setErgName(request.ergName());
        erg.setFocus(request.focus());
        erg.setMission(request.mission());
        erg.setFoundedDate(request.foundedDate());
        erg.setStatus(request.status());

        if (!leadScoped) {
            UserResponse lead = userClient.getByIdInternal(request.ergLeadId());
            if (lead.role() != Role.ERG_LEAD) {
                throw new ConflictException("The Group Head must be a user with the ERG Lead role");
            }
            erg.setErgLeadId(request.ergLeadId());
            if (request.executiveSponsorId() != null) {
                userClient.getByIdInternal(request.executiveSponsorId());
            }
            erg.setExecutiveSponsorId(request.executiveSponsorId());
        }

        erg = ergRepository.save(erg);
        auditLogWriter.record("UPDATE_ERG", "ERG", erg.getId());
        return ergMapper.toResponse(erg);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ErgResponse> search(ErgFocus focus, ErgStatus status, Pageable pageable) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();

        Page<ERG> page = switch (principal.getRole()) {
            case EMPLOYEE -> ergRepository.searchVisibleToEmployee(
                    focus, status, resolveManagerId(principal.getId()), pageable);
            case DEI_MANAGER -> ergRepository.searchVisibleToEmployee(
                    focus, status, principal.getId(), pageable);
            case ERG_LEAD -> ergRepository.searchForLead(focus, status, principal.getId(), pageable);
            default -> ergRepository.search(focus, status, null, pageable);
        };
        return page.map(ergMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ErgResponse getById(Long id) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        ERG erg = findOrThrow(id);
        if (!visibleTo(erg, principal)) {
            throw new ResourceNotFoundException("ERG", id);
        }
        return ergMapper.toResponse(erg);
    }

    private boolean visibleTo(ERG erg, DeiUserPrincipal principal) {
        return switch (principal.getRole()) {
            case EMPLOYEE -> visibleToEmployee(erg, principal.getId());
            case DEI_MANAGER -> erg.getCreatorManagerId() == null
                    || erg.getCreatorManagerId().equals(principal.getId());
            case ERG_LEAD -> Objects.equals(erg.getErgLeadId(), principal.getId());
            default -> true;
        };
    }

    private boolean visibleToEmployee(ERG erg, Long employeeUserId) {
        if (erg.getCreatorManagerId() == null) {
            return true;
        }
        Long managerId = resolveManagerId(employeeUserId);
        return managerId != null && erg.getCreatorManagerId().equals(managerId);
    }

    private Long resolveManagerId(Long employeeUserId) {
        try {
            return userClient.getByIdInternal(employeeUserId).managerId();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void assertEmployeeCanAccess(Long ergId) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() != Role.EMPLOYEE) {
            return;
        }
        ERG erg = findOrThrow(ergId);
        if (!visibleToEmployee(erg, principal.getId())) {
            throw new ForbiddenOperationException("You may only join ERG groups run by your manager");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ERG erg = findOrThrow(id);
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() == Role.DEI_MANAGER) {
            if (erg.getCreatorManagerId() == null || !erg.getCreatorManagerId().equals(principal.getId())) {
                throw new ForbiddenOperationException("You may only delete ERG groups you created");
            }
        }
        ergRepository.delete(erg);
        auditLogWriter.record("DELETE_ERG", "ERG", id);
    }

    @Override
    public ERG findOrThrow(Long id) {
        return ergRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ERG", id));
    }

    @Override
    public ERG requireManageableChapter(Long ergId) {
        ERG erg = findOrThrow(ergId);
        Role role = SecurityUtils.getCurrentRole();
        if (role == Role.ADMIN) {
            return erg;
        }
        if (role == Role.DEI_MANAGER) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (erg.getCreatorManagerId() == null || !erg.getCreatorManagerId().equals(currentUserId)) {
                throw new ForbiddenOperationException("You may only manage ERG groups you created");
            }
            return erg;
        }
        if (role == Role.ERG_LEAD) {
            ensureOwnChapter(erg);
            return erg;
        }
        throw new ForbiddenOperationException("You are not permitted to manage this chapter");
    }

    @Override
    @Transactional
    public void recomputeMemberCount(Long ergId) {
        ERG erg = findOrThrow(ergId);
        int active = membershipRepository.countByErg_IdAndStatus(ergId, MembershipStatus.ACTIVE);
        erg.setMemberCount(active);
        ergRepository.save(erg);
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveMemberCount(String scope, String scopeValue, Long hrId) {
        java.util.List<Long> activeMemberIds = membershipRepository.findActiveMemberUserIds();
        if (activeMemberIds.isEmpty()) {
            return 0L;
        }

        java.util.List<UserResponse> members = userClient.getByIdsInternal(activeMemberIds);

        long count = 0;
        for (UserResponse member : members) {
            if (!"ACTIVE".equalsIgnoreCase(member.status())) {
                continue;
            }

            if (hrId != null && !hrId.equals(member.hrId())) {
                continue;
            }

            if ("DEPARTMENT".equalsIgnoreCase(scope) && scopeValue != null) {
                try {
                    Long deptId = Long.parseLong(scopeValue);
                    if (!deptId.equals(member.departmentId())) {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            } else if ("GRADE".equalsIgnoreCase(scope) && scopeValue != null) {
                try {
                    Long grId = Long.parseLong(scopeValue);
                    if (!grId.equals(member.gradeId())) {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            count++;
        }
        return count;
    }

    private void ensureOwnChapter(ERG erg) {
        Long leadId = erg.getErgLeadId();
        if (!Objects.equals(leadId, SecurityUtils.getCurrentUserId())) {
            throw new ForbiddenOperationException("You may only manage your own ERG chapter");
        }
    }
}
