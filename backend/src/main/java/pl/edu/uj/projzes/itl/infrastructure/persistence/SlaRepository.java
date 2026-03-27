package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.sla.SlaPolicy;

import java.util.Optional;

public interface SlaRepository extends JpaRepository<SlaPolicy, Long> {

    Optional<SlaPolicy> findByProjectIdAndPriority(String projectId, IncidentPriority priority);
}
