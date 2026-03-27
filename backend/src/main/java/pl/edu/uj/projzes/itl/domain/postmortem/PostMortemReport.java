package pl.edu.uj.projzes.itl.domain.postmortem;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "post_mortem_reports")
public class PostMortemReport {

    @Id
    private String id;

    private String incidentId;

    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String timeline;

    @Column(columnDefinition = "TEXT")
    private String impact;

    @Column(columnDefinition = "TEXT")
    private String actionItems;

    private String author;
    private Instant createdAt;
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private PostMortemStatus status;

    // TODO: getters/setters via Lombok
}
