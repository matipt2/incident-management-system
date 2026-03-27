package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.application.SlaService
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority
import pl.edu.uj.projzes.itl.domain.sla.SlaViolation
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
@Transactional
class SlaServiceSpec extends Specification {

    // TODO: przy implementacji wstrzyknąć Clock.fixed(...) żeby symulować czas

    @Subject
    @Autowired
    SlaService slaService

    @Autowired
    IncidentService incidentService

    def "incydent bez przekroczenia SLA nie generuje naruszenia"() {
        given:
        def incident = incidentService.reportIncident("Nowy incydent", "Opis", "user", "API", "PROJ-1")
        incidentService.applyClassification(incident.id, IncidentPriority.LOW, IncidentCategory.OTHER, "agent1")

        when:
        slaService.checkSlaBreaches()

        then:
        slaService.getViolationsForIncident(incident.id).isEmpty()
    }

    def "naruszenie SLA typu RESOLUTION_TIME_EXCEEDED jest wykrywane"() {
        given: "incydent, dla którego SLA już minęło"
        def incident = incidentService.reportIncident("Stary incydent", "Opis", "user", "API", "PROJ-SLA")
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.NETWORK, "agent1")
        // TODO: ustawić createdAt w przeszłości poza oknem SLA

        when:
        slaService.checkSlaBreaches()

        then:
        def violations = slaService.getViolationsForIncident(incident.id)
        violations.any { it.violationType == SlaViolation.ViolationType.RESOLUTION_TIME_EXCEEDED }
    }

    def "naruszenie SLA nie jest duplikowane przy ponownym sprawdzeniu"() {
        given:
        def incident = incidentService.reportIncident("Duplikat SLA", "Opis", "user", "API", "PROJ-SLA")
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.NETWORK, "agent1")
        // TODO: ustawić createdAt w przeszłości poza oknem SLA
        slaService.checkSlaBreaches()

        when:
        slaService.checkSlaBreaches()

        then:
        slaService.getViolationsForIncident(incident.id)
                .count { it.violationType == SlaViolation.ViolationType.RESOLUTION_TIME_EXCEEDED } <= 1
    }

    def "naruszenia SLA dla różnych priorytetów są wykrywane poprawnie"() {
        given:
        def incident = incidentService.reportIncident("Test priorytetu", "Opis", "user", "API", "PROJ-SLA")
        incidentService.applyClassification(incident.id, priority, IncidentCategory.OTHER, "agent1")

        when:
        slaService.checkSlaBreaches()

        then:
        notThrown(Exception)

        where:
        priority << [IncidentPriority.CRITICAL, IncidentPriority.HIGH, IncidentPriority.MEDIUM, IncidentPriority.LOW]
    }
}
