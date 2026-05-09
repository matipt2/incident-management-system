package pl.edu.uj.projzes.itl.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.uj.projzes.itl.application.SlaService;

@Component
@ConditionalOnProperty(name = "itl.sla.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SlaScheduler {

    private final SlaService slaService;

    public SlaScheduler(SlaService slaService) {
        this.slaService = slaService;
    }

    @Scheduled(
            initialDelayString = "${itl.sla.scheduler.initial-delay-ms:60000}",
            fixedDelayString = "${itl.sla.scheduler.fixed-delay-ms:300000}"
    )
    public void checkSlaBreaches() {
        slaService.checkSlaBreaches();
    }
}
