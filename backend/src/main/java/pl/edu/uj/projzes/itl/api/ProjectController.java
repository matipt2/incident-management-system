package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.uj.projzes.itl.api.dto.ProjectRequest;
import pl.edu.uj.projzes.itl.api.dto.ProjectResponse;
import pl.edu.uj.projzes.itl.api.dto.ProjectStatusRequest;
import pl.edu.uj.projzes.itl.api.dto.ProjectUpdateRequest;
import pl.edu.uj.projzes.itl.application.ProjectService;
import pl.edu.uj.projzes.itl.application.ProjectNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_READ')")
    public List<ProjectResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        if (includeInactive) {
            requireProjectWrite();
        }
        return projectService.getProjects(includeInactive).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('PROJECT_READ')")
    public ProjectResponse get(@PathVariable String key) {
        var project = projectService.getByKey(key);
        if (!project.isActive() && !hasProjectWrite()) {
            throw new ProjectNotFoundException(key);
        }
        return ProjectResponse.from(project);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PROJECT_WRITE')")
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return ProjectResponse.from(
                projectService.create(request.key(), request.name(), request.description())
        );
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('PROJECT_WRITE')")
    public ProjectResponse update(@PathVariable String key,
                                  @Valid @RequestBody ProjectUpdateRequest request) {
        return ProjectResponse.from(projectService.update(key, request.name(), request.description()));
    }

    @PatchMapping("/{key}/status")
    @PreAuthorize("hasAuthority('PROJECT_WRITE')")
    public ProjectResponse setStatus(@PathVariable String key,
                                     @Valid @RequestBody ProjectStatusRequest request) {
        return ProjectResponse.from(projectService.setActive(key, request.active()));
    }

    private void requireProjectWrite() {
        if (!hasProjectWrite()) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private boolean hasProjectWrite() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "PROJECT_WRITE".equals(authority.getAuthority()));
    }
}
