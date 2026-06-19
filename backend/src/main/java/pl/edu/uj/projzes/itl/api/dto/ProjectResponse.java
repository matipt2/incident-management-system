package pl.edu.uj.projzes.itl.api.dto;

import pl.edu.uj.projzes.itl.domain.project.Project;

import java.time.Instant;

public record ProjectResponse(
        String key,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getKey(),
                project.getName(),
                project.getDescription(),
                project.isActive(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
