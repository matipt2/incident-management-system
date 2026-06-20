package pl.edu.uj.projzes.itl.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import pl.edu.uj.projzes.itl.api.dto.SlaPolicyRequest;
import pl.edu.uj.projzes.itl.api.dto.SlaPolicyResponse;
import pl.edu.uj.projzes.itl.api.dto.SlaViolationResponse;
import pl.edu.uj.projzes.itl.application.SlaService;
import pl.edu.uj.projzes.itl.application.IncidentVisibilityService;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.infrastructure.web.CurrentUserProvider;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sla")
public class SlaController {

    private final SlaService slaService;
    private final IncidentVisibilityService incidentVisibilityService;
    private final CurrentUserProvider currentUserProvider;

    public SlaController(SlaService slaService,
                         IncidentVisibilityService incidentVisibilityService,
                         CurrentUserProvider currentUserProvider) {
        this.slaService = slaService;
        this.incidentVisibilityService = incidentVisibilityService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/check")
    @PreAuthorize("hasAuthority('SLA_WRITE')")
    public List<SlaViolationResponse> checkViolations() {
        slaService.checkSlaBreaches();
        return slaService.getViolations(null).stream()
                .map(SlaViolationResponse::from)
                .toList();
    }

    @GetMapping("/policies")
    @PreAuthorize("hasAuthority('SLA_READ')")
    public List<SlaPolicyResponse> listPolicies() {
        return slaService.getPolicies().stream()
                .map(SlaPolicyResponse::from)
                .toList();
    }

    @PostMapping("/policies")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SLA_WRITE')")
    public SlaPolicyResponse createPolicy(@Valid @RequestBody SlaPolicyRequest request) {
        return SlaPolicyResponse.from(
                slaService.createPolicy(
                        request.projectId(),
                        request.priority(),
                        Duration.ofMinutes(request.responseTimeMinutes()),
                        Duration.ofMinutes(request.resolutionTimeMinutes()),
                        request.penaltyAmount()
                )
        );
    }

    @PutMapping("/policies/{id}")
    @PreAuthorize("hasAuthority('SLA_WRITE')")
    public SlaPolicyResponse updatePolicy(@PathVariable Long id,
                                          @Valid @RequestBody SlaPolicyRequest request) {
        return SlaPolicyResponse.from(
                slaService.updatePolicy(
                        id,
                        request.projectId(),
                        request.priority(),
                        Duration.ofMinutes(request.responseTimeMinutes()),
                        Duration.ofMinutes(request.resolutionTimeMinutes()),
                        request.penaltyAmount()
                )
        );
    }

    @GetMapping("/violations")
    @PreAuthorize("hasAuthority('SLA_READ')")
    public List<SlaViolationResponse> listViolations(
            @RequestParam(required = false) String projectId) {
        var user = currentUserProvider.get();
        var visibleIds = incidentVisibilityService.getVisibleIncidents(
                user, null, null, null, projectId, null)
                .stream()
                .map(Incident::getId)
                .collect(Collectors.toSet());
        return slaService.getViolations(projectId).stream()
                .filter(violation -> visibleIds.contains(violation.getIncidentId()))
                .map(SlaViolationResponse::from)
                .toList();
    }

    @GetMapping("/violations/{incidentId}")
    @PreAuthorize("hasAuthority('SLA_READ')")
    public List<SlaViolationResponse> violationsForIncident(
            @PathVariable String incidentId) {
        incidentVisibilityService.getVisibleIncident(incidentId, currentUserProvider.get());
        return slaService.getViolationsForIncident(incidentId).stream()
                .map(SlaViolationResponse::from)
                .toList();
    }

    @PostMapping("/violations/{id}/apply-penalty")
    @PreAuthorize("hasAuthority('SLA_WRITE')")
    public SlaViolationResponse applyPenalty(@PathVariable Long id) {
        return SlaViolationResponse.from(slaService.applyPenalty(id));
    }
}
