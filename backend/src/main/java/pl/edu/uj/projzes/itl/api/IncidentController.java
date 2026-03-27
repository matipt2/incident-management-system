package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.ClassifyRequest;
import pl.edu.uj.projzes.itl.api.dto.CreateIncidentRequest;
import pl.edu.uj.projzes.itl.api.dto.IncidentResponse;
import pl.edu.uj.projzes.itl.application.ClassificationService;
import pl.edu.uj.projzes.itl.application.IncidentService;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.infrastructure.llm.ClassificationResult;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final ClassificationService classificationService;

    public IncidentController(IncidentService incidentService,
                               ClassificationService classificationService) {
        this.incidentService = incidentService;
        this.classificationService = classificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @GetMapping
    public List<IncidentResponse> list(@RequestParam(required = false) IncidentStatus status) {
        throw new UnsupportedOperationException("TODO");
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/{id}/assign")
    public IncidentResponse assign(@PathVariable String id, @RequestParam String agentId) {
        throw new UnsupportedOperationException("TODO");
    }

    @GetMapping("/{id}/classify/preview")
    public ClassificationResult classifyPreview(@PathVariable String id) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/{id}/classify")
    public IncidentResponse classify(@PathVariable String id,
                                     @Valid @RequestBody ClassifyRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/{id}/escalate")
    public IncidentResponse escalate(@PathVariable String id,
                                     @RequestParam String reason,
                                     @RequestParam String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/{id}/resolve")
    public IncidentResponse resolve(@PathVariable String id,
                                    @RequestParam String resolution,
                                    @RequestParam String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/{id}/close")
    public IncidentResponse close(@PathVariable String id,
                                   @RequestParam String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }
}
