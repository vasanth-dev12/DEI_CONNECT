package com.deiconnect.erg.service;

import com.deiconnect.erg.dto.CreateEventRequest;
import com.deiconnect.erg.dto.EventResponse;
import com.deiconnect.erg.dto.UpdateEventRequest;
import com.deiconnect.erg.dto.EventParticipationResponse;
import com.deiconnect.erg.dto.ParticipantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ErgEventService {

    EventResponse create(Long ergId, CreateEventRequest request);

    EventResponse update(Long ergId, Long eventId, UpdateEventRequest request);

    void delete(Long ergId, Long eventId);

    Page<EventResponse> list(Long ergId, Pageable pageable);

    EventResponse getById(Long ergId, Long eventId);

    EventParticipationResponse participate(Long ergId, Long eventId);

    void cancelParticipation(Long ergId, Long eventId);

    List<ParticipantResponse> listParticipants(Long ergId, Long eventId);
}
