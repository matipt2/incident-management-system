package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.PostMortemRequest;
import pl.edu.uj.projzes.itl.application.PostMortemService;
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemReport;

@RestController
@RequestMapping("/api/incidents/{incidentId}/post-mortem")
public class PostMortemController {

    private final PostMortemService postMortemService;

    public PostMortemController(PostMortemService postMortemService) {
        this.postMortemService = postMortemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostMortemReport create(@PathVariable String incidentId,
                                   @RequestParam String author) {
        throw new UnsupportedOperationException("TODO");
    }

    @GetMapping
    public PostMortemReport get(@PathVariable String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }

    @PutMapping
    public PostMortemReport update(@PathVariable String incidentId,
                                   @Valid @RequestBody PostMortemRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    @PostMapping("/approve")
    public PostMortemReport approve(@PathVariable String incidentId,
                                    @RequestParam String approver) {
        throw new UnsupportedOperationException("TODO");
    }
}
