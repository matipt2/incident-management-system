package pl.edu.uj.projzes.itl.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.uj.projzes.itl.domain.user.User;
import pl.edu.uj.projzes.itl.domain.user.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(UserRole role);

    List<User> findAllByOrderByUsernameAsc();

    long countByRole(UserRole role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
