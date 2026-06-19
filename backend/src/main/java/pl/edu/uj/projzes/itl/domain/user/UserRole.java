package pl.edu.uj.projzes.itl.domain.user;

import java.util.EnumSet;
import java.util.Set;

import static pl.edu.uj.projzes.itl.domain.user.Permission.*;

public enum UserRole {

    VIEWER(EnumSet.of(
            INCIDENT_READ,
            PROJECT_READ
    )),

    REPORTER(EnumSet.of(
            INCIDENT_REPORT,
            INCIDENT_READ,
            PROJECT_READ
    )),

    AGENT(EnumSet.of(
            INCIDENT_REPORT,
            INCIDENT_READ,
            INCIDENT_CLASSIFY,
            INCIDENT_ESCALATE,
            INCIDENT_RESOLVE,
            SLA_READ,
            POSTMORTEM_READ,
            PROJECT_READ
    )),

    MANAGER(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
