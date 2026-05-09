package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;

import java.math.BigDecimal;

public record SlaPolicyRequest(
        @NotBlank String projectId,
        @NotNull IncidentPriority priority,
        @NotNull @Min(1) Long responseTimeMinutes,
        @NotNull @Min(1) Long resolutionTimeMinutes,
        @NotNull BigDecimal penaltyAmount
) {}
