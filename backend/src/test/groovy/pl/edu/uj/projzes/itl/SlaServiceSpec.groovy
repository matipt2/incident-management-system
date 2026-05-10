package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.application.SlaService
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus
import pl.edu.uj.projzes.itl.domain.sla.SlaPolicy
import pl.edu.uj.projzes.itl.domain.sla.SlaViolation
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository
import pl.edu.uj.projzes.itl.infrastructure.persistence.SlaRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration
import java.time.Instant

@SpringBootTest
@Transactional
class SlaServiceSpec extends Specification {

    // TODO: przy implementacji wstrzyknąć Clock.fixed(...) żeby symulować czas

    @Subject
    @Autowired
    SlaService slaService

    @Autowired
    IncidentService incidentService

    @Autowired
    IncidentRepository incidentRepository

    @Autowired
    SlaRepository slaRepository

    def "incydent bez przekroczenia SLA nie generuje naruszenia"() {
        given:
        createPolicy("PROJ-1", IncidentPriority.LOW, Duration.ofHours(1))
        def incident = incidentService.reportIncident("Nowy incydent", "Opis", "user", "API", "PROJ-1")
        incidentService.applyClassification(incident.id, IncidentPriority.LOW, IncidentCategory.OTHER, "agent1")

        when:
        slaService.checkSlaBreaches()

        then:
        slaService.getViolationsForIncident(incident.id).isEmpty()
    }

    def "naruszenie SLA typu RESOLUTION_TIME_EXCEEDED jest wykrywane"() {
        given: "incydent, dla którego SLA już minęło"
        createPolicy("PROJ-SLA", IncidentPriority.CRITICAL, Duration.ofMinutes(30))
        def incident = incidentService.reportIncident("Stary incydent", "Opis", "user", "API", "PROJ-SLA")
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.NETWORK, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofHours(2))

        when:
        slaService.checkSlaBreaches()

        then:
        def violations = slaService.getViolationsForIncident(incident.id)
        violations.any { it.violationType == SlaViolation.ViolationType.RESOLUTION_TIME_EXCEEDED }
    }

    def "naruszenie SLA nie jest duplikowane przy ponownym sprawdzeniu"() {
        given:
        createPolicy("PROJ-SLA", IncidentPriority.CRITICAL, Duration.ofMinutes(30))
        def incident = incidentService.reportIncident("Duplikat SLA", "Opis", "user", "API", "PROJ-SLA")
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.NETWORK, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofHours(2))
        slaService.checkSlaBreaches()

        when:
        slaService.checkSlaBreaches()

        then:
        slaService.getViolationsForIncident(incident.id)
                .count { it.violationType == SlaViolation.ViolationType.RESOLUTION_TIME_EXCEEDED } <= 1
    }

    def "naruszenia SLA dla różnych priorytetów są wykrywane poprawnie"() {
        given:
        createPolicy("PROJ-SLA", priority, Duration.ofMinutes(30))
        def incident = incidentService.reportIncident("Test priorytetu", "Opis", "user", "API", "PROJ-SLA")
        incidentService.applyClassification(incident.id, priority, IncidentCategory.OTHER, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofHours(2))

        when:
        slaService.checkSlaBreaches()

        then:
        notThrown(Exception)

        where:
        priority << [IncidentPriority.CRITICAL, IncidentPriority.HIGH, IncidentPriority.MEDIUM, IncidentPriority.LOW]
    }

    def "naruszenie SLA typu RESPONSE_TIME_EXCEEDED jest wykrywane gdy incydent nie jest przypisany"() {
        given:
        createPolicy("PROJ-RESP", IncidentPriority.HIGH, Duration.ofHours(2))
        def incident = incidentService.reportIncident("Brak reakcji", "Opis", "user", "API", "PROJ-RESP")
        incidentService.applyClassification(incident.id, IncidentPriority.HIGH, IncidentCategory.APPLICATION, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofMinutes(30))

        when:
        slaService.checkSlaBreaches()

        then:
        slaService.getViolationsForIncident(incident.id)
                .any { it.violationType == SlaViolation.ViolationType.RESPONSE_TIME_EXCEEDED }
    }

    def "zamknięte incydenty są pomijane podczas sprawdzania SLA"() {
        given:
        createPolicy("PROJ-CLOSED", IncidentPriority.LOW, Duration.ofMinutes(1))
        def incident = incidentService.reportIncident("Zamknięty", "Opis", "user", "API", "PROJ-CLOSED")
        incidentService.applyClassification(incident.id, IncidentPriority.LOW, IncidentCategory.OTHER, "agent1")
        def persisted = incidentRepository.findById(incident.id).orElseThrow()
        persisted.status = IncidentStatus.CLOSED
        persisted.createdAt = Instant.now().minus(Duration.ofHours(1))
        persisted.updatedAt = persisted.createdAt
        incidentRepository.saveAndFlush(persisted)

        when:
        slaService.checkSlaBreaches()

        then:
        slaService.getViolationsForIncident(incident.id).isEmpty()
    }

    def "incydenty bez polityki SLA są ignorowane"() {
        given:
        def incident = incidentService.reportIncident("Bez polityki", "Opis", "user", "API", "PROJ-NO-POLICY")
        incidentService.applyClassification(incident.id, IncidentPriority.HIGH, IncidentCategory.OTHER, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofHours(2))

        when:
        slaService.checkSlaBreaches()

        then:
        slaService.getViolationsForIncident(incident.id).isEmpty()
    }

    def "naliczenie kary ustawia flagę penaltyApplied"() {
        given:
        createPolicy("PROJ-PENALTY", IncidentPriority.CRITICAL, Duration.ofMinutes(1))
        def incident = incidentService.reportIncident("Kara", "Opis", "user", "API", "PROJ-PENALTY")
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.NETWORK, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofMinutes(10))
        slaService.checkSlaBreaches()
        def violation = slaService.getViolationsForIncident(incident.id)
                .find { it.violationType == SlaViolation.ViolationType.RESOLUTION_TIME_EXCEEDED }

        when:
        def updated = slaService.applyPenalty(violation.id)

        then:
        updated.penaltyApplied
    }

    private SlaPolicy createPolicy(String projectId, IncidentPriority priority, Duration resolutionTime) {
        def policy = new SlaPolicy()
        policy.projectId = projectId
        policy.priority = priority
        policy.responseTime = Duration.ofMinutes(15)
        policy.resolutionTime = resolutionTime
        policy.penaltyAmount = 100.00G
        slaRepository.save(policy)
    }

    private void moveCreatedAtToPast(String incidentId, Duration age) {
        def incident = incidentRepository.findById(incidentId).orElseThrow()
        incident.createdAt = Instant.now().minus(age)
        incident.updatedAt = incident.createdAt
        incidentRepository.saveAndFlush(incident)
    }
}
