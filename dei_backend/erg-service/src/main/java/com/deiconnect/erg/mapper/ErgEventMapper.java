package com.deiconnect.erg.mapper;

import com.deiconnect.erg.dto.EventResponse;
import com.deiconnect.erg.entity.ERGEvent;
import org.springframework.stereotype.Component;

@Component
public class ErgEventMapper {
    public EventResponse toResponse(ERGEvent event) {
        return new EventResponse(
                event.getId(),
                event.getErg() == null ? null : event.getErg().getId(),
                event.getEventName(),
                event.getEventType(),
                event.getDate(),
                event.getAttendeeCount(),
                event.getBudgetSpent(),
                event.getStatus(),
                event.getCreatedDate(),
                event.getLastModifiedDate());
    }
}
