package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.user.Permission;
import pl.edu.uj.projzes.itl.domain.user.UserRole;

import java.util.Set;

public record MeResponse(
        String userId,
        String username,
        UserRole role,
        Set<Permission> permissions
) {}
