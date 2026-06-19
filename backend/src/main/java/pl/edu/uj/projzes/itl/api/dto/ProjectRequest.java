package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9_-]{1,49}")
        String key,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 4000) String description
) {
}
