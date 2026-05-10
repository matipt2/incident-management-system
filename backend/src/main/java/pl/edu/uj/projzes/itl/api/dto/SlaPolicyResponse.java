package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.sla.SlaPolicy;

import java.math.BigDecimal;

public record SlaPolicyResponse(
        Long id,
        String projectId,
        IncidentPriority priority,
        long responseTimeMinutes,
        long resolutionTimeMinutes,
        BigDecimal penaltyAmount
) {
    public static SlaPolicyResponse from(SlaPolicy policy) {
        return new SlaPolicyResponse(
                policy.getId(),
                policy.getProjectId(),
                policy.getPriority(),
                policy.getResponseTime().toMinutes(),
                policy.getResolutionTime().toMinutes(),
                policy.getPenaltyAmount()
        );
    }
}
