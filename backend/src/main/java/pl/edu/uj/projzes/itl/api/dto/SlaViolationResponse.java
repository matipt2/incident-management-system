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
) {}
