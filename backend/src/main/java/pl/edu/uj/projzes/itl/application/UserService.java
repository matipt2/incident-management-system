package pl.edu.uj.projzes.itl.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.projzes.itl.api.dto.AuthResponse;
import pl.edu.uj.projzes.itl.api.dto.LoginRequest;
import pl.edu.uj.projzes.itl.api.dto.RegisterRequest;
import pl.edu.uj.projzes.itl.api.dto.UserResponse;
import pl.edu.uj.projzes.itl.domain.user.User;
import pl.edu.uj.projzes.itl.domain.user.UserRole;
import pl.edu.uj.projzes.itl.infrastructure.persistence.UserRepository;
import pl.edu.uj.projzes.itl.infrastructure.security.JwtService;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                UserRole.REPORTER
        );
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                user.getId().toString(),
                user.getUsername(),
                user.getRole(),
                user.getRole().getPermissions()
        );
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional
    public User updateRole(UUID userId, UserRole role, String performedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (user.getUsername().equals(performedBy) && user.getRole() != role) {
            throw new UserRoleChangeException("Managers cannot change their own role");
        }
        if (user.getRole() == UserRole.MANAGER
                && role != UserRole.MANAGER
                && userRepository.countByRole(UserRole.MANAGER) <= 1) {
            throw new UserRoleChangeException("The final manager cannot be demoted");
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User createBootstrapManager(String username, String email, String password) {
        if (userRepository.countByRole(UserRole.MANAGER) > 0) {
            return null;
        }
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Bootstrap manager username or email already exists");
        }
        User manager = new User(
                username.trim(),
                email.trim(),
                passwordEncoder.encode(password),
                UserRole.MANAGER
        );
        return userRepository.save(manager);
    }
}
