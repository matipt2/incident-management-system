package pl.edu.uj.projzes.itl.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.uj.projzes.itl.application.UserService;

@Configuration
public class BootstrapManagerConfig {

    @Bean
    ApplicationRunner bootstrapManager(
            UserService userService,
            @Value("${itl.bootstrap-manager.username:}") String username,
            @Value("${itl.bootstrap-manager.email:}") String email,
            @Value("${itl.bootstrap-manager.password:}") String password) {
        return args -> {
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                return;
            }
            userService.createBootstrapManager(username, email, password);
        };
    }
}
