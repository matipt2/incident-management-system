package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PostMortemRequest(
        @NotBlank String rootCause,
        @NotBlank String timeline,
        @NotBlank String impact,
        @NotBlank String actionItems,
        @NotBlank String author
) {}
