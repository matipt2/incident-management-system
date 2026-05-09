package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.incident.IncidentEvent;

import java.time.Instant;
import java.util.List;

public record IncidentHistoryResponse(
        String incidentId,
        List<EventDto> events
) {
    public record EventDto(
            String eventType,
            String details,
            String performedBy,
            Instant occurredAt
    ) {
        public static EventDto from(IncidentEvent event) {
            return new EventDto(
                    event.getEventType(),
                    event.getDetails(),
                    event.getPerformedBy(),
                    event.getOccurredAt()
            );
        }
    }

    public static IncidentHistoryResponse of(String incidentId, List<IncidentEvent> events) {
        return new IncidentHistoryResponse(
                incidentId,
                events.stream().map(EventDto::from).toList()
        );
    }
}
