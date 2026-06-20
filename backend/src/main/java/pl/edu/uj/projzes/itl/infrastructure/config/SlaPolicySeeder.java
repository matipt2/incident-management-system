package pl.edu.uj.projzes.itl.infrastructure.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import pl.edu.uj.projzes.itl.application.SlaService;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.infrastructure.persistence.SlaRepository;

import java.math.BigDecimal;
import java.time.Duration;

@Configuration
public class SlaPolicySeeder {

    @Bean
    @Order(100)
    ApplicationRunner seedDefaultSlaPolicies(SlaRepository slaRepository, SlaService slaService) {
        return args -> {
            if (slaRepository.count() > 0) {
                return;
            }

            seedProject(slaService, "PROJ-1");
            seedProject(slaService, "PROJ-SLA");
        };
    }

    private void seedProject(SlaService slaService, String projectId) {
        slaService.createPolicy(projectId, IncidentPriority.CRITICAL,
                Duration.ofMinutes(15), Duration.ofHours(2), new BigDecimal("1000.00"));
        slaService.createPolicy(projectId, IncidentPriority.HIGH,
                Duration.ofMinutes(30), Duration.ofHours(8), new BigDecimal("500.00"));
        slaService.createPolicy(projectId, IncidentPriority.MEDIUM,
                Duration.ofHours(2), Duration.ofHours(24), new BigDecimal("200.00"));
        slaService.createPolicy(projectId, IncidentPriority.LOW,
                Duration.ofHours(8), Duration.ofHours(72), new BigDecimal("50.00"));
    }
}
