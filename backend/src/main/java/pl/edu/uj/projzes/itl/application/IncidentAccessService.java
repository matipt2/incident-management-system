package pl.edu.uj.projzes.itl.application;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.domain.user.UserRole;

@Service
public class IncidentAccessService {

    public void requireAgentOrManager(Incident incident, CurrentUser user) {
        if (user.role() == UserRole.MANAGER) {
            return;
        }
        if (user.role() == UserRole.AGENT && user.username().equals(incident.getAssignedTo())) {
            return;
        }
        throw new AccessDeniedException("Incident is not assigned to the current user");
    }

    public void requireManager(CurrentUser user) {
        if (user.role() != UserRole.MANAGER) {
            throw new AccessDeniedException("Only managers can perform this action");
        }
    }
}
