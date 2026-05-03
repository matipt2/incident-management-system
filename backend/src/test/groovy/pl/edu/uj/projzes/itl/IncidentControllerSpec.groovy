package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.api.IncidentController
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus
import spock.lang.Specification

@SpringBootTest
@Transactional
@WithMockUser(authorities = ["INCIDENT_READ", "INCIDENT_REPORT"])
class IncidentControllerSpec extends Specification {

    @Autowired
    IncidentController incidentController

    @Autowired
    IncidentService incidentService

    def "list bez filtra deleguje do getAll i zwraca wszystkie incydenty"() {
        given:
        incidentService.reportIncident("Inc A", "Opis", "user", "API", "PROJ-1")
        incidentService.reportIncident("Inc B", "Opis", "user", "API", "PROJ-2")

        when:
        def result = incidentController.list(null)

        then:
        result.size() >= 2
    }

    def "list z filtrem deleguje do getByStatus i zwraca tylko pasujące incydenty"() {
        given:
        incidentService.reportIncident("Inc new", "Opis", "user", "API", "PROJ-1")

        when:
        def result = incidentController.list(IncidentStatus.NEW)

        then:
        !result.isEmpty()
        result.every { it.status() == IncidentStatus.NEW }
    }
}
