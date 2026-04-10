package pl.edu.uj.projzes.itl.domain.user;

public record CurrentUser(String userId, UserRole role) {

    public boolean hasPermission(Permission permission) {
        return role.hasPermission(permission);
    }
}
