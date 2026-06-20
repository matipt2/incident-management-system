package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.ClassifyRequest;
import pl.edu.uj.projzes.itl.api.dto.CreateIncidentRequest;
import pl.edu.uj.projzes.itl.api.dto.IncidentEventResponse;
import pl.edu.uj.projzes.itl.api.dto.IncidentResponse;
import pl.edu.uj.projzes.itl.application.IncidentService;
import pl.edu.uj.projzes.itl.application.IncidentAccessService;
import pl.edu.uj.projzes.itl.application.IncidentVisibilityService;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.infrastructure.web.CurrentUserProvider;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentVisibilityService incidentVisibilityService;
    private final IncidentAccessService incidentAccessService;
    private final CurrentUserProvider currentUserProvider;

    public IncidentController(IncidentService incidentService,
                              IncidentVisibilityService incidentVisibilityService,
                              IncidentAccessService incidentAccessService,
                              CurrentUserProvider currentUserProvider) {
        this.incidentService = incidentService;
        this.incidentVisibilityService = incidentVisibilityService;
        this.incidentAccessService = incidentAccessService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INCIDENT_REPORT')")
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request) {
        return IncidentResponse.from(
                incidentService.reportIncident(
                        request.title(),
                        request.description(),
                        currentUser().username(),
                        request.channel(),
                        request.projectId()
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public List<IncidentResponse> list(@RequestParam(required = false) IncidentStatus status) {
        return incidentVisibilityService.getVisibleIncidents(
                        currentUser(), status, null, null, null, null)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public IncidentResponse get(@PathVariable String id) {
        return IncidentResponse.from(incidentVisibilityService.getVisibleIncident(id, currentUser()));
    }

    @GetMapping("/{id}/events")
    @PreAuthorize("hasAuthority('INCIDENT_READ')")
    public List<IncidentEventResponse> events(
            @PathVariable String id,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return incidentVisibilityService.getVisibleIncidentHistory(id, currentUser()).stream()
                .filter(event -> eventType == null || eventType.isBlank()
                        || eventType.equals(event.getEventType()))
                .filter(event -> from == null || !event.getOccurredAt().isBefore(from))
                .filter(event -> to == null || !event.getOccurredAt().isAfter(to))
                .map(IncidentEventResponse::from)
                .toList();
    }

    @PostMapping("/{id}/classify")
    @PreAuthorize("hasAuthority('INCIDENT_CLASSIFY')")
    public IncidentResponse classify(@PathVariable String id,
                                     @Valid @RequestBody ClassifyRequest request) {
        CurrentUser user = currentUser();
        var incident = incidentVisibilityService.getVisibleIncident(id, user);
        incidentAccessService.requireAgentOrManager(incident, user);
        return IncidentResponse.from(
                incidentService.applyClassification(id, request.priority(), request.category(), user.username())
        );
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAuthority('INCIDENT_ESCALATE')")
    public IncidentResponse escalate(@PathVariable String id, @RequestParam String reason) {
        CurrentUser user = currentUser();
        var incident = incidentVisibilityService.getVisibleIncident(id, user);
        incidentAccessService.requireAgentOrManager(incident, user);
        return IncidentResponse.from(incidentService.escalate(id, reason, user.username()));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('INCIDENT_RESOLVE')")
    public IncidentResponse resolve(@PathVariable String id, @RequestParam String resolution) {
        CurrentUser user = currentUser();
        var incident = incidentVisibilityService.getVisibleIncident(id, user);
        incidentAccessService.requireAgentOrManager(incident, user);
        return IncidentResponse.from(incidentService.resolve(id, resolution, user.username()));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('INCIDENT_CLOSE')")
    public IncidentResponse close(@PathVariable String id) {
        CurrentUser user = currentUser();
        var incident = incidentVisibilityService.getVisibleIncident(id, user);
        incidentAccessService.requireManager(user);
        return IncidentResponse.from(incidentService.close(incident.getId(), user.username()));
    }

    private CurrentUser currentUser() {
        return currentUserProvider.get();
    }
}
