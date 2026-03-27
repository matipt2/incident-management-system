package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
@Transactional
class IncidentServiceSpec extends Specification {

    @Subject
    @Autowired
    IncidentService incidentService

    // US1: Zgłoszenie incydentu
    def "zgłoszony incydent otrzymuje unikalny id i status NEW"() {
        when:
        def incident = incidentService.reportIncident("Serwer niedostępny", "Opis", "jan.kowalski", "API", "PROJ-1")

        then:
        incident.id != null
        incident.status == IncidentStatus.NEW
        incident.createdAt != null
    }

    def "zgłoszony incydent zapisuje metadane (kanał, autor, projekt)"() {
        when:
        def incident = incidentService.reportIncident("Błąd DB", "Timeout", "anna.nowak", "EMAIL", "PROJ-2")

        then:
        incident.reportedBy == "anna.nowak"
        incident.channel == "EMAIL"
        incident.projectId == "PROJ-2"
    }

    def "każde zgłoszenie tworzy zdarzenie INCIDENT_OPENED w historii"() {
        when:
        def incident = incidentService.reportIncident("VPN nie działa", "Brak dostępu", "piotr.w", "FORM", "PROJ-1")

        then:
        incident.events.any { it.eventType == "INCIDENT_OPENED" }
    }

    // US2: Przypisanie do agenta
    def "przypisanie incydentu zmienia status na IN_PROGRESS"() {
        given:
        def incident = incidentService.reportIncident("Problem z siecią", "Opis", "user", "API", "PROJ-1")

        when:
        def assigned = incidentService.assignToAgent(incident.id, "agent.support1")

        then:
        assigned.status == IncidentStatus.IN_PROGRESS
        assigned.assignedTo == "agent.support1"
    }

    // US3: Historia zdarzeń
    def "historia incydentu zawiera wszystkie zdarzenia w kolejności chronologicznej"() {
        given:
        def incident = incidentService.reportIncident("Awaria", "Opis", "user", "API", "PROJ-1")
        incidentService.assignToAgent(incident.id, "agent1")

        when:
        def fetched = incidentService.getById(incident.id)

        then:
        fetched.events.size() >= 2
        fetched.events[0].occurredAt <= fetched.events[1].occurredAt
    }

    // US5: Zatwierdzenie / nadpisanie klasyfikacji LLM
    def "agent może nadpisać klasyfikację incydentu"() {
        given:
        def incident = incidentService.reportIncident("Atak DDoS", "Przeciążenie sieci", "user", "API", "PROJ-1")

        when:
        def classified = incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.SECURITY, "agent1")

        then:
        classified.priority == IncidentPriority.CRITICAL
        classified.category == IncidentCategory.SECURITY
        classified.events.any { it.eventType == "CLASSIFICATION_APPLIED" }
    }

    def "nadpisanie klasyfikacji jest zapisane w historii incydentu"() {
        given:
        def incident = incidentService.reportIncident("Błąd aplikacji", "NPE w produkcji", "user", "API", "PROJ-1")

        when:
        incidentService.applyClassification(incident.id, IncidentPriority.HIGH, IncidentCategory.APPLICATION, "agent1")
        def fetched = incidentService.getById(incident.id)

        then:
        fetched.events.any { it.eventType == "CLASSIFICATION_APPLIED" && it.performedBy == "agent1" }
    }

    // Eskalacja
    def "eskalacja zmienia status incydentu na ESCALATED"() {
        given:
        def incident = incidentService.reportIncident("Krytyczna awaria", "Opis", "user", "API", "PROJ-1")

        when:
        def escalated = incidentService.escalate(incident.id, "Zbliża się deadline SLA", "kierownik")

        then:
        escalated.status == IncidentStatus.ESCALATED
        escalated.events.any { it.eventType == "ESCALATED" }
    }

    // Rozwiązanie
    def "rozwiązanie incydentu ustawia status RESOLVED i czas resolvedAt"() {
        given:
        def incident = incidentService.reportIncident("Awaria serwera", "Opis", "user", "API", "PROJ-1")
        incidentService.assignToAgent(incident.id, "agent1")

        when:
        def resolved = incidentService.resolve(incident.id, "Restart serwera pomógł", "agent1")

        then:
        resolved.status == IncidentStatus.RESOLVED
        resolved.resolvedAt != null
    }

    // Filtrowanie
    def "filtrowanie po statusie zwraca tylko incydenty z danym statusem"() {
        given:
        incidentService.reportIncident("Inc A", "Opis", "user", "API", "PROJ-1")

        when:
        def results = incidentService.getByStatus(IncidentStatus.NEW)

        then:
        results.every { it.status == IncidentStatus.NEW }
    }
}
