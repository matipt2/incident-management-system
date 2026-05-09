package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.uj.projzes.itl.domain.sla.SlaViolation;

import java.util.List;

public interface SlaViolationRepository extends JpaRepository<SlaViolation, Long> {

    List<SlaViolation> findByIncidentId(String incidentId);

    List<SlaViolation> findByProjectId(String projectId);

    List<SlaViolation> findByProjectIdAndPenaltyApplied(String projectId, boolean penaltyApplied);

    boolean existsByIncidentIdAndViolationType(String incidentId, SlaViolation.ViolationType violationType);
}
