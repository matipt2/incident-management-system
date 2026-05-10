package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.sla.SlaViolation;

import java.math.BigDecimal;
import java.time.Instant;

public record SlaViolationResponse(
        Long id,
        String incidentId,
        String projectId,
        SlaViolation.ViolationType violationType,
        Instant detectedAt,
        BigDecimal penalty,
        boolean penaltyApplied
) {
    public static SlaViolationResponse from(SlaViolation violation) {
        return new SlaViolationResponse(
                violation.getId(),
                violation.getIncidentId(),
                violation.getProjectId(),
                violation.getViolationType(),
                violation.getDetectedAt(),
                violation.getPenalty(),
                violation.isPenaltyApplied()
        );
    }
}
