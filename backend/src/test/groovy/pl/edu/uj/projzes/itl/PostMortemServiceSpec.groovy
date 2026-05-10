package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.application.PostMortemNotFoundException
import pl.edu.uj.projzes.itl.application.PostMortemService
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemStatus
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
@Transactional
class PostMortemServiceSpec extends Specification {

    @Subject
    @Autowired
    PostMortemService postMortemService

    @Autowired
    IncidentService incidentService

    def "tworzenie raportu post-mortem dla incydentu krytycznego"() {
        given:
        def incident = incidentService.reportIncident("Awaria krytyczna", "Opis", "user", "API", "PROJ-1")

        when:
        def report = postMortemService.create(incident.id, "author")

        then:
        report.id != null
        report.incidentId == incident.id
        report.status == PostMortemStatus.DRAFT
    }

    def "aktualizacja raportu post-mortem"() {
        given:
        def incident = incidentService.reportIncident("Awaria", "Opis", "user", "API", "PROJ-1")
        postMortemService.create(incident.id, "author")

        when:
        def updated = postMortemService.update(incident.id, "Root cause", "Timeline", "Impact", "Action items", "author")

        then:
        updated.rootCause == "Root cause"
        updated.status == PostMortemStatus.DRAFT
    }

    def "zatwierdzenie raportu zmienia status na APPROVED"() {
        given:
        def incident = incidentService.reportIncident("Awaria", "Opis", "user", "API", "PROJ-1")
        postMortemService.create(incident.id, "author")
        postMortemService.update(incident.id, "Root cause", "Timeline", "Impact", "Action items", "author")

        when:
        def approved = postMortemService.approve(incident.id, "manager")

        then:
        approved.status == PostMortemStatus.APPROVED
    }

    def "pobranie brakującego raportu zwraca błąd domenowy"() {
        when:
        postMortemService.getByIncidentId("missing-incident")

        then:
        thrown(PostMortemNotFoundException)
    }

    def "nie można zatwierdzić nieuzupełnionego raportu"() {
        given:
        def incident = incidentService.reportIncident("Awaria", "Opis", "user", "API", "PROJ-1")
        postMortemService.create(incident.id, "author")

        when:
        postMortemService.approve(incident.id, "manager")

        then:
        thrown(IllegalStateException)
    }

    def "ponowne utworzenie raportu dla incydentu zwraca istniejący raport"() {
        given:
        def incident = incidentService.reportIncident("Awaria", "Opis", "user", "API", "PROJ-1")
        def first = postMortemService.create(incident.id, "author")

        when:
        def second = postMortemService.create(incident.id, "other-author")

        then:
        second.id == first.id
        second.author == "author"
    }
}
