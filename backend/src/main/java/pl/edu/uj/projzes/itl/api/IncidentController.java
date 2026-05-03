package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.ClassifyRequest;
import pl.edu.uj.projzes.itl.api.dto.CreateIncidentRequest;
import pl.edu.uj.projzes.itl.api.dto.IncidentResponse;
import pl.edu.uj.projzes.itl.application.IncidentService;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.infrastructure.web.UserContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INCIDENT_REPORT')")
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request) {
        return IncidentResponse.from(
                incidentService.reportIncident(
                        request.title(),
                        request.description(),
                        currentUsername(),
                        request.channel(),
                        request.projectId()
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public List<IncidentResponse> list(@RequestParam(required = false) IncidentStatus status) {
        var incidents = status != null
                ? incidentService.getByStatus(status)
                : incidentService.getAll();
        return incidents.stream().map(IncidentResponse::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public IncidentResponse get(@PathVariable String id) {
        return IncidentResponse.from(incidentService.getById(id));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('INCIDENT_ASSIGN')")
    public IncidentResponse assign(@PathVariable String id, @RequestParam String agentId) {
        return IncidentResponse.from(incidentService.assignToAgent(id, agentId));
    }

    @PostMapping("/{id}/classify")
    @PreAuthorize("hasAuthority('INCIDENT_CLASSIFY')")
    public IncidentResponse classify(@PathVariable String id,
                                     @Valid @RequestBody ClassifyRequest request) {
        return IncidentResponse.from(
                incidentService.applyClassification(id, request.priority(), request.category(), currentUsername())
        );
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAuthority('INCIDENT_ESCALATE')")
    public IncidentResponse escalate(@PathVariable String id, @RequestParam String reason) {
        return IncidentResponse.from(incidentService.escalate(id, reason, currentUsername()));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('INCIDENT_RESOLVE')")
    public IncidentResponse resolve(@PathVariable String id, @RequestParam String resolution) {
        return IncidentResponse.from(incidentService.resolve(id, resolution, currentUsername()));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('INCIDENT_CLOSE')")
    public IncidentResponse close(@PathVariable String id) {
        return IncidentResponse.from(incidentService.close(id, currentUsername()));
    }

    private String currentUsername() {
        CurrentUser user = UserContextHolder.get();
        if (user != null) {
            return user.username();
        }
        // Fallback for tests using @WithMockUser (no JWT filter)
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
