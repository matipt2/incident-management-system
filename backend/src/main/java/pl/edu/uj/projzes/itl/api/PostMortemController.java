package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.PostMortemRequest;
import pl.edu.uj.projzes.itl.api.dto.PostMortemResponse;
import pl.edu.uj.projzes.itl.application.PostMortemService;
import pl.edu.uj.projzes.itl.application.IncidentVisibilityService;
import pl.edu.uj.projzes.itl.infrastructure.web.CurrentUserProvider;

@RestController
@RequestMapping("/api/incidents/{incidentId}/post-mortem")
public class PostMortemController {

    private final PostMortemService postMortemService;
    private final IncidentVisibilityService incidentVisibilityService;
    private final CurrentUserProvider currentUserProvider;

    public PostMortemController(PostMortemService postMortemService,
                                IncidentVisibilityService incidentVisibilityService,
                                CurrentUserProvider currentUserProvider) {
        this.postMortemService = postMortemService;
        this.incidentVisibilityService = incidentVisibilityService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('POSTMORTEM_WRITE')")
    public PostMortemResponse create(@PathVariable String incidentId) {
        requireVisible(incidentId);
        return PostMortemResponse.from(postMortemService.create(incidentId, currentUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('POSTMORTEM_READ')")
    public PostMortemResponse get(@PathVariable String incidentId) {
        requireVisible(incidentId);
        return PostMortemResponse.from(postMortemService.getByIncidentId(incidentId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('POSTMORTEM_WRITE')")
    public PostMortemResponse update(@PathVariable String incidentId,
                                     @Valid @RequestBody PostMortemRequest request) {
        requireVisible(incidentId);
        return PostMortemResponse.from(
                postMortemService.update(
                        incidentId,
                        request.rootCause(),
                        request.timeline(),
                        request.impact(),
                        request.actionItems(),
                        currentUsername()
                )
        );
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('POSTMORTEM_APPROVE')")
    public PostMortemResponse approve(@PathVariable String incidentId) {
        requireVisible(incidentId);
        return PostMortemResponse.from(postMortemService.approve(incidentId, currentUsername()));
    }

    private String currentUsername() {
        return currentUserProvider.get().username();
    }

    private void requireVisible(String incidentId) {
        incidentVisibilityService.getVisibleIncident(incidentId, currentUserProvider.get());
    }
}
