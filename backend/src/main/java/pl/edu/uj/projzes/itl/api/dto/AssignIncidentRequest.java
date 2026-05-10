package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignIncidentRequest(@NotBlank String agentId) {
}
