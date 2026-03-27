package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemReport;

import java.util.Optional;

public interface PostMortemRepository extends JpaRepository<PostMortemReport, String> {

    Optional<PostMortemReport> findByIncidentId(String incidentId);
}
