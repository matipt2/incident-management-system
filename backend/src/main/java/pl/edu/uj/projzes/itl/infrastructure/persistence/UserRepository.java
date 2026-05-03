package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.uj.projzes.itl.domain.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
