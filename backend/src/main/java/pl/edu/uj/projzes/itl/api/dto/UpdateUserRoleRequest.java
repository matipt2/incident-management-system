package pl.edu.uj.projzes.itl.api.dto;

import jakarta.validation.constraints.NotNull;
import pl.edu.uj.projzes.itl.domain.user.UserRole;

public record UpdateUserRoleRequest(@NotNull UserRole role) {
}
