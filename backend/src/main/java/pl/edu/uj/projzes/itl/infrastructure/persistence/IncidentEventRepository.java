package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.uj.projzes.itl.domain.incident.IncidentEvent;

import java.util.List;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, String> {

    List<IncidentEvent> findByIncidentIdOrderByOccurredAtAsc(String incidentId);
}
