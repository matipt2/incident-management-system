package pl.edu.uj.projzes.itl.domain.sla;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sla_violations")
public class SlaViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String incidentId;
    private String projectId;

    @Enumerated(EnumType.STRING)
    private ViolationType violationType;

    private Instant detectedAt;
    private BigDecimal penalty;
    private boolean penaltyApplied;

    public enum ViolationType {
        RESPONSE_TIME_EXCEEDED,
        RESOLUTION_TIME_EXCEEDED
    }

}
