package pl.edu.uj.projzes.itl.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.uj.projzes.itl.api.dto.AuthResponse;
import pl.edu.uj.projzes.itl.api.dto.LoginRequest;
import pl.edu.uj.projzes.itl.api.dto.RegisterRequest;
import pl.edu.uj.projzes.itl.api.dto.UserResponse;
import pl.edu.uj.projzes.itl.application.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
