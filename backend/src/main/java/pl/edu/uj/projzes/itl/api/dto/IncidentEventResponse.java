package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.incident.IncidentEvent;

import java.time.Instant;

public record IncidentEventResponse(
        String id,
        String eventType,
        String details,
        String performedBy,
        Instant occurredAt
) {
    public static IncidentEventResponse from(IncidentEvent event) {
        return new IncidentEventResponse(
                event.getId(),
                event.getEventType(),
                event.getDetails(),
                event.getPerformedBy(),
                event.getOccurredAt()
        );
    }
}
