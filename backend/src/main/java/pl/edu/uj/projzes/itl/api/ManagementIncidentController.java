package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.uj.projzes.itl.api.dto.AgentResponse;
import pl.edu.uj.projzes.itl.api.dto.AssignIncidentRequest;
import pl.edu.uj.projzes.itl.api.dto.IncidentResponse;
import pl.edu.uj.projzes.itl.application.IncidentService;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.domain.user.UserRole;
import pl.edu.uj.projzes.itl.infrastructure.persistence.UserRepository;
import pl.edu.uj.projzes.itl.infrastructure.web.CurrentUserProvider;

import java.util.List;

@RestController
@RequestMapping("/api/management")
public class ManagementIncidentController {

    private final UserRepository userRepository;
    private final IncidentService incidentService;
    private final CurrentUserProvider currentUserProvider;

    public ManagementIncidentController(UserRepository userRepository,
                                        IncidentService incidentService,
                                        CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.incidentService = incidentService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/agents")
    @PreAuthorize("hasAuthority('INCIDENT_ASSIGN')")
    public List<AgentResponse> listAgents() {
        ensureManager(currentUser());
        return userRepository.findByRole(UserRole.AGENT).stream()
                .map(user -> new AgentResponse(user.getId().toString(), user.getUsername(), user.getEmail()))
                .toList();
    }

    @PostMapping("/incidents/{id}/assignment")
    @PreAuthorize("hasAuthority('INCIDENT_ASSIGN')")
    public IncidentResponse assignIncident(@PathVariable String id,
                                           @Valid @RequestBody AssignIncidentRequest request) {
        ensureManager(currentUser());

        var agent = userRepository.findByUsername(request.agentId())
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + request.agentId()));

        if (agent.getRole() != UserRole.AGENT) {
            throw new IllegalArgumentException("User is not an AGENT: " + request.agentId());
        }

        return IncidentResponse.from(
                incidentService.assignToAgent(id, agent.getUsername(), currentUser().username())
        );
    }

    private CurrentUser currentUser() {
        return currentUserProvider.get();
    }

    private void ensureManager(CurrentUser user) {
        if (user.role() != UserRole.MANAGER) {
            throw new AccessDeniedException(
                    "Only manager can use management incident endpoints"
            );
        }
    }
}
