package pl.edu.uj.projzes.itl.infrastructure.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import pl.edu.uj.projzes.itl.application.SlaService;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentEvent;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;
import pl.edu.uj.projzes.itl.infrastructure.persistence.SlaRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Configuration
@ConditionalOnProperty(name = "itl.demo-data.enabled", havingValue = "true")
public class DemoDataSeeder {

    static final String DEMO_INCIDENT_ID = "DEMO-SLA-1";

    @Bean
    @Order(200)
    public ApplicationRunner seedDemoSlaViolation(
            IncidentRepository incidentRepository,
            SlaRepository slaRepository,
            SlaService slaService,
            Clock clock) {
        return args -> {
            ensureCriticalPolicyExists(slaRepository, slaService);
            incidentRepository.findById(DEMO_INCIDENT_ID)
                    .orElseGet(() -> incidentRepository.save(createDemoIncident(clock.instant())));
            slaService.checkSlaBreaches();
        };
    }

    private void ensureCriticalPolicyExists(SlaRepository slaRepository, SlaService slaService) {
        slaRepository.findFirstByProjectIdAndPriorityOrderByIdAsc("PROJ-SLA", IncidentPriority.CRITICAL)
                .orElseGet(() -> slaService.createPolicy(
                        "PROJ-SLA",
                        IncidentPriority.CRITICAL,
                        Duration.ofMinutes(15),
                        Duration.ofHours(2),
                        new BigDecimal("1000.00")
                ));
    }

    private Incident createDemoIncident(Instant now) {
        Instant reportedAt = now.minus(Duration.ofHours(3));

        Incident incident = new Incident();
        incident.setId(DEMO_INCIDENT_ID);
        incident.setTitle("Demo checkout outage exceeding SLA");
        incident.setDescription("Seeded critical incident used to demonstrate SLA violations and penalties.");
        incident.setReportedBy("demo-reporter");
        incident.setChannel("DEMO_SEED");
        incident.setProjectId("PROJ-SLA");
        incident.setStatus(IncidentStatus.NEW);
        incident.setPriority(IncidentPriority.CRITICAL);
        incident.setCategory(IncidentCategory.APPLICATION);
        incident.setCreatedAt(reportedAt);
        incident.setUpdatedAt(reportedAt);

        IncidentEvent event = new IncidentEvent();
        event.setId("DEMO-SLA-1-REPORTED");
        event.setIncident(incident);
        event.setEventType("INCIDENT_REPORTED");
        event.setDetails("Seeded demo incident reported three hours ago");
        event.setPerformedBy("demo-seeder");
        event.setOccurredAt(reportedAt);
        incident.getEvents().add(event);

        return incident;
    }
}
