package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotNull;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;

public record ClassifyRequest(
        @NotNull IncidentPriority priority,
        @NotNull IncidentCategory category,
        @NotNull String performedBy
) {}
