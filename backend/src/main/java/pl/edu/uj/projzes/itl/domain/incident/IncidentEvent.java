package pl.edu.uj.projzes.itl.domain.incident;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "incident_events")
public class IncidentEvent {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String performedBy;
    private Instant occurredAt;

}
