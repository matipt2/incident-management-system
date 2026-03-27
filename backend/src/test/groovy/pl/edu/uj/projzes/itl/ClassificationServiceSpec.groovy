package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.application.ClassificationService
import pl.edu.uj.projzes.itl.application.IncidentService
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
@Transactional
class ClassificationServiceSpec extends Specification {

    @Subject
    @Autowired
    ClassificationService classificationService

    @Autowired
    IncidentService incidentService

    // US4: Rekomendacja klasyfikacji bez zmiany stanu
    def "podgląd klasyfikacji zwraca rekomendację priorytetu i kategorii"() {
        when:
        def result = classificationService.preview("Serwer niedostępny", "Produkcja nie odpowiada od 10 minut")

        then:
        result != null
        result.suggestedPriority() != null
        result.suggestedCategory() != null
    }

    def "podgląd klasyfikacji nie zmienia stanu incydentu"() {
        given:
        def incident = incidentService.reportIncident("Błąd sieci", "Opis", "user", "API", "PROJ-1")

        when:
        classificationService.preview(incident.title, incident.description)
        def fetched = incidentService.getById(incident.id)

        then:
        fetched.priority == null
        fetched.category == null
    }

    def "klasyfikacja asynchroniczna zapisuje zdarzenie LLM_CLASSIFICATION_SUGGESTED w historii"() {
        given:
        def incident = incidentService.reportIncident("Atak na sieć", "Duże obciążenie", "user", "API", "PROJ-1")

        when:
        classificationService.classifyAsync(incident.id)
        def fetched = incidentService.getById(incident.id)

        then:
        fetched.events.any { it.eventType == "LLM_CLASSIFICATION_SUGGESTED" }
    }
}
