package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemReport;
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemStatus;

import java.time.Instant;

public record PostMortemResponse(
        String id,
        String incidentId,
        String rootCause,
        String timeline,
        String impact,
        String actionItems,
        String author,
        PostMortemStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostMortemResponse from(PostMortemReport report) {
        return new PostMortemResponse(
                report.getId(),
                report.getIncidentId(),
                report.getRootCause(),
                report.getTimeline(),
                report.getImpact(),
                report.getActionItems(),
                report.getAuthor(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
