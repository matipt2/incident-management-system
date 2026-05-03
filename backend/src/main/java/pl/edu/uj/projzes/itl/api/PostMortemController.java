package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.PostMortemRequest;
import pl.edu.uj.projzes.itl.api.dto.PostMortemResponse;
import pl.edu.uj.projzes.itl.application.PostMortemService;

@RestController
@RequestMapping("/api/incidents/{incidentId}/post-mortem")
public class PostMortemController {

    private final PostMortemService postMortemService;

    public PostMortemController(PostMortemService postMortemService) {
        this.postMortemService = postMortemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('POSTMORTEM_WRITE')")
    public PostMortemResponse create(@PathVariable String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('POSTMORTEM_READ')")
    public PostMortemResponse get(@PathVariable String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }

    @PutMapping
    @PreAuthorize("hasAuthority('POSTMORTEM_WRITE')")
    public PostMortemResponse update(@PathVariable String incidentId,
                                     @Valid @RequestBody PostMortemRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('POSTMORTEM_APPROVE')")
    public PostMortemResponse approve(@PathVariable String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }
}
