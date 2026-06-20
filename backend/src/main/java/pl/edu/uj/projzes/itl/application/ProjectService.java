package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.projzes.itl.domain.project.Project;
import pl.edu.uj.projzes.itl.infrastructure.persistence.ProjectRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final Clock clock;

    public ProjectService(ProjectRepository projectRepository, Clock clock) {
        this.projectRepository = projectRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Project> getProjects(boolean includeInactive) {
        return includeInactive
                ? projectRepository.findAllByOrderByNameAsc()
                : projectRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Project getByKey(String key) {
        return projectRepository.findById(normalizeKey(key))
                .orElseThrow(() -> new ProjectNotFoundException(key));
    }

    @Transactional(readOnly = true)
    public Project requireActive(String key) {
        Project project = getByKey(key);
        if (!project.isActive()) {
            throw new ProjectInactiveException(project.getKey());
        }
        return project;
    }

    @Transactional
    public Project create(String key, String name, String description) {
        String normalizedKey = normalizeKey(key);
        if (projectRepository.existsById(normalizedKey)) {
            throw new ProjectAlreadyExistsException(normalizedKey);
        }

        Instant now = clock.instant();
        Project project = new Project();
        project.setKey(normalizedKey);
        project.setName(name.trim());
        project.setDescription(trimToNull(description));
        project.setActive(true);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(String key, String name, String description) {
        Project project = getByKey(key);
        project.setName(name.trim());
        project.setDescription(trimToNull(description));
        project.setUpdatedAt(clock.instant());
        return projectRepository.save(project);
    }

    @Transactional
    public Project setActive(String key, boolean active) {
        Project project = getByKey(key);
        project.setActive(active);
        project.setUpdatedAt(clock.instant());
        return projectRepository.save(project);
    }

    private String normalizeKey(String key) {
        return key == null ? null : key.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
