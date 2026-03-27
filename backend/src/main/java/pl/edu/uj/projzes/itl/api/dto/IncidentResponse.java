package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;

import java.time.Instant;
import java.util.List;

public record IncidentResponse(
        String id,
        String title,
        String description,
        String reportedBy,
        String channel,
        String projectId,
        IncidentStatus status,
        IncidentPriority priority,
        IncidentCategory category,
        String assignedTo,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt,
        List<EventDto> events
) {
    public record EventDto(
            String eventType,
            String details,
            String performedBy,
            Instant occurredAt
    ) {}

    // TODO: metoda mapująca Incident -> IncidentResponse
}
