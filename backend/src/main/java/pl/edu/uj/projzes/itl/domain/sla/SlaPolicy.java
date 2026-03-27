package pl.edu.uj.projzes.itl.domain.sla;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import java.math.BigDecimal;
import java.time.Duration;

@Getter
@Setter
@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectId;

    @Enumerated(EnumType.STRING)
    private IncidentPriority priority;

    private Duration responseTime;
    private Duration resolutionTime;
    private BigDecimal penaltyAmount;

}
