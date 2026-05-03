package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.edu.uj.projzes.itl.domain.user.UserRole;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        UserRole role
) {
    public UserRole roleOrDefault() {
        return role != null ? role : UserRole.REPORTER;
    }
}
