package pl.edu.uj.projzes.itl

import pl.edu.uj.projzes.itl.application.SlaService
import pl.edu.uj.projzes.itl.infrastructure.config.SlaScheduler
import spock.lang.Specification

class SlaSchedulerSpec extends Specification {

    def "scheduler deleguje sprawdzanie naruszeń SLA do serwisu"() {
        given:
        def slaService = Mock(SlaService)
        def scheduler = new SlaScheduler(slaService)

        when:
        scheduler.checkSlaBreaches()

        then:
        1 * slaService.checkSlaBreaches()
    }
}
