package pl.edu.uj.projzes.itl.api;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.uj.projzes.itl.api.dto.IncidentHistoryResponse;
import pl.edu.uj.projzes.itl.api.dto.IncidentResponse;
import pl.edu.uj.projzes.itl.application.IncidentVisibilityService;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.domain.user.User;
import pl.edu.uj.projzes.itl.infrastructure.web.UserContextHolder;
import pl.edu.uj.projzes.itl.infrastructure.persistence.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/my/incidents")
public class MyIncidentController {

    private final IncidentVisibilityService incidentVisibilityService;
    private final UserRepository userRepository;

    public MyIncidentController(IncidentVisibilityService incidentVisibilityService,
                                UserRepository userRepository) {
        this.incidentVisibilityService = incidentVisibilityService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public List<IncidentResponse> listVisible(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentPriority priority,
            @RequestParam(required = false) IncidentCategory category,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String search
    ) {
        return incidentVisibilityService.getVisibleIncidents(currentUser(), status, priority, category, projectId, search)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public IncidentResponse getVisible(@PathVariable String id) {
        return IncidentResponse.from(incidentVisibilityService.getVisibleIncident(id, currentUser()));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public IncidentHistoryResponse history(@PathVariable String id) {
        return IncidentHistoryResponse.of(id, incidentVisibilityService.getVisibleIncidentHistory(id, currentUser()));
    }

    private CurrentUser currentUser() {
        CurrentUser user = UserContextHolder.get();
        if (user == null) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User resolved = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AccessDeniedException("No user context"));
            return new CurrentUser(resolved.getId().toString(), resolved.getUsername(), resolved.getRole());
        }
        return user;
    }
}
