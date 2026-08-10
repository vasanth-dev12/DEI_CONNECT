package com.deiconnect.erg.controller;

import com.deiconnect.erg.dto.CreateEventRequest;
import com.deiconnect.erg.dto.EventResponse;
import com.deiconnect.erg.dto.UpdateEventRequest;
import com.deiconnect.erg.dto.EventParticipationResponse;
import com.deiconnect.erg.dto.ParticipantResponse;
import com.deiconnect.erg.service.ErgEventService;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ergs/{ergId}/events")
@RequiredArgsConstructor
public class ErgEventController {

    private final ErgEventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ERG_LEAD')")
    public ResponseEntity<EventResponse> create(@PathVariable Long ergId,
                                                @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(ergId, request));
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ERG_LEAD')")
    public ResponseEntity<EventResponse> update(@PathVariable Long ergId,
                                               @PathVariable Long eventId,
                                               @Valid @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.update(ergId, eventId, request));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ERG_LEAD')")
    public ResponseEntity<Void> delete(@PathVariable Long ergId, @PathVariable Long eventId) {
        eventService.delete(ergId, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','DEI_MANAGER','HR_BIZ_PARTNER','ERG_LEAD','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<EventResponse>> list(@PathVariable Long ergId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(eventService.list(ergId, pageable));
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','DEI_MANAGER','HR_BIZ_PARTNER','ERG_LEAD','EXECUTIVE','ADMIN')")
    public ResponseEntity<EventResponse> getById(@PathVariable Long ergId, @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getById(ergId, eventId));
    }

    @PostMapping("/{eventId}/participate")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EventParticipationResponse> participate(@PathVariable Long ergId,
                                                                  @PathVariable Long eventId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.participate(ergId, eventId));
    }

    @DeleteMapping("/{eventId}/participate")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> cancelParticipation(@PathVariable Long ergId,
                                                    @PathVariable Long eventId) {
        eventService.cancelParticipation(ergId, eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/participants")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','ERG_LEAD','ADMIN')")
    public ResponseEntity<List<ParticipantResponse>> listParticipants(@PathVariable Long ergId,
                                                                      @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.listParticipants(ergId, eventId));
    }
}
