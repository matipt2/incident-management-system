package pl.edu.uj.projzes.itl.domain.incident;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    private String id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String reportedBy;
    private String channel;
    private String projectId;
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    @Enumerated(EnumType.STRING)
    private IncidentPriority priority;

    @Enumerated(EnumType.STRING)
    private IncidentCategory category;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("occurredAt ASC")
    private List<IncidentEvent> events = new ArrayList<>();

}
