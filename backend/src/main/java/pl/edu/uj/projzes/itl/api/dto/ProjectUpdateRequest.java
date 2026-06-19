package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 4000) String description
) {
}
