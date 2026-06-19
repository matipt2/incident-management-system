package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotNull;

public record ProjectStatusRequest(@NotNull Boolean active) {
}
