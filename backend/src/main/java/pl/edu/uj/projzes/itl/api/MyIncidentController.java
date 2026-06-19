package pl.edu.uj.projzes.itl.api;

import org.springframework.security.access.prepost.PreAuthorize;
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
import pl.edu.uj.projzes.itl.infrastructure.web.CurrentUserProvider;

import java.util.List;

@RestController
@RequestMapping("/api/my/incidents")
public class MyIncidentController {

    private final IncidentVisibilityService incidentVisibilityService;
    private final CurrentUserProvider currentUserProvider;

    public MyIncidentController(IncidentVisibilityService incidentVisibilityService,
                                CurrentUserProvider currentUserProvider) {
        this.incidentVisibilityService = incidentVisibilityService;
        this.currentUserProvider = currentUserProvider;
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
        return currentUserProvider.get();
    }
}
