package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.ClassifyRequest;
import pl.edu.uj.projzes.itl.api.dto.CreateIncidentRequest;
import pl.edu.uj.projzes.itl.api.dto.IncidentResponse;
import pl.edu.uj.projzes.itl.application.IncidentService;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;

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
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request) {
        return IncidentResponse.from(
                incidentService.reportIncident(
                        request.title(),
                        request.description(),
                        request.reportedBy(),
                        request.channel(),
                        request.projectId()
                )
        );
    }

    @GetMapping
    public List<IncidentResponse> list(@RequestParam(required = false) IncidentStatus status) {
        var incidents = status != null
                ? incidentService.getByStatus(status)
                : incidentService.getAll();
        return incidents.stream().map(IncidentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable String id) {
        return IncidentResponse.from(incidentService.getById(id));
    }

    @PostMapping("/{id}/assign")
    public IncidentResponse assign(@PathVariable String id, @RequestParam String agentId) {
        return IncidentResponse.from(incidentService.assignToAgent(id, agentId));
    }

    @PostMapping("/{id}/classify")
    public IncidentResponse classify(@PathVariable String id,
                                     @Valid @RequestBody ClassifyRequest request) {
        return IncidentResponse.from(
                incidentService.applyClassification(id, request.priority(), request.category(), request.performedBy())
        );
    }

    @PostMapping("/{id}/escalate")
    public IncidentResponse escalate(@PathVariable String id,
                                     @RequestParam String reason,
                                     @RequestParam String performedBy) {
        return IncidentResponse.from(incidentService.escalate(id, reason, performedBy));
    }

    @PostMapping("/{id}/resolve")
    public IncidentResponse resolve(@PathVariable String id,
                                    @RequestParam String resolution,
                                    @RequestParam String performedBy) {
        return IncidentResponse.from(incidentService.resolve(id, resolution, performedBy));
    }

    @PostMapping("/{id}/close")
    public IncidentResponse close(@PathVariable String id,
                                   @RequestParam String performedBy) {
        return IncidentResponse.from(incidentService.close(id, performedBy));
    }
}
