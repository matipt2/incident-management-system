package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.incident.Incident;
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

    public static IncidentResponse from(Incident incident) {
        List<EventDto> events = incident.getEvents().stream()
                .map(e -> new EventDto(e.getEventType(), e.getDetails(), e.getPerformedBy(), e.getOccurredAt()))
                .toList();
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getReportedBy(),
                incident.getChannel(),
                incident.getProjectId(),
                incident.getStatus(),
                incident.getPriority(),
                incident.getCategory(),
                incident.getAssignedTo(),
                incident.getCreatedAt(),
                incident.getUpdatedAt(),
                incident.getResolvedAt(),
                events
        );
    }
}
