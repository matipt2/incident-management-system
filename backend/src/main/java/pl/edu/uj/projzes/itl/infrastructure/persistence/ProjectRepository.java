package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.uj.projzes.itl.domain.project.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByActiveTrueOrderByNameAsc();

    List<Project> findAllByOrderByNameAsc();
}
