package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.user.Permission;
import pl.edu.uj.projzes.itl.domain.user.User;
import pl.edu.uj.projzes.itl.domain.user.UserRole;

import java.util.Set;

public record UserResponse(
        String userId,
        String username,
        String email,
        UserRole role,
        Set<Permission> permissions
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getRole().getPermissions()
        );
    }
}
