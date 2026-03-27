package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, String>,
        JpaSpecificationExecutor<Incident> {

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findByPriority(IncidentPriority priority);

    List<Incident> findByProjectId(String projectId);

    List<Incident> findByAssignedTo(String agentId);
}
