package pl.edu.uj.projzes.itl.api;

import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.SlaViolationResponse;
import pl.edu.uj.projzes.itl.application.SlaService;

import java.util.List;

@RestController
@RequestMapping("/api/sla")
public class SlaController {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/violations")
    public List<SlaViolationResponse> listViolations(
            @RequestParam(required = false) String projectId) {
        throw new UnsupportedOperationException("TODO");
    }

    @GetMapping("/violations/{incidentId}")
    public List<SlaViolationResponse> violationsForIncident(
            @PathVariable String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/violations/{id}/apply-penalty")
    public SlaViolationResponse applyPenalty(@PathVariable Long id,
                                             @RequestParam String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }
}
