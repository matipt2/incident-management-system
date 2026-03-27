package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateIncidentRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String reportedBy,
        @NotBlank String channel,
        @NotBlank String projectId
) {}
