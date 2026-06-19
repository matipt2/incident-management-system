package pl.edu.uj.projzes.itl.infrastructure.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.domain.user.User;
import pl.edu.uj.projzes.itl.domain.user.UserRole;
import pl.edu.uj.projzes.itl.infrastructure.persistence.UserRepository;

import java.util.Collection;

@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CurrentUser get() {
        CurrentUser currentUser = UserContextHolder.get();
        if (currentUser != null) {
            return currentUser;
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(user -> new CurrentUser(user.getId().toString(), user.getUsername(), user.getRole()))
                .orElseGet(() -> mockUser(username));
    }

    private CurrentUser mockUser(String username) {
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        UserRole role;
        if (has(authorities, "USER_MANAGE")
                || has(authorities, "INCIDENT_CLOSE")
                || has(authorities, "INCIDENT_ASSIGN")
                || has(authorities, "SLA_WRITE")
                || has(authorities, "POSTMORTEM_WRITE")
                || has(authorities, "POSTMORTEM_APPROVE")) {
            role = UserRole.MANAGER;
        } else if (has(authorities, "INCIDENT_CLASSIFY")
                || has(authorities, "INCIDENT_RESOLVE")
                || has(authorities, "INCIDENT_ESCALATE")
                || has(authorities, "SLA_READ")
                || has(authorities, "POSTMORTEM_READ")) {
            role = UserRole.AGENT;
        } else if (has(authorities, "INCIDENT_REPORT")) {
            role = UserRole.REPORTER;
        } else if (has(authorities, "INCIDENT_READ")) {
            role = UserRole.VIEWER;
        } else {
            throw new AccessDeniedException("No user context");
        }
        return new CurrentUser("test-user", username, role);
    }

    private boolean has(
            Collection<? extends GrantedAuthority> authorities,
            String authority) {
        return authorities.stream().anyMatch(item -> authority.equals(item.getAuthority()));
    }
}
