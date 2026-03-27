package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import pl.edu.uj.projzes.itl.domain.sla.SlaViolation;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;
import pl.edu.uj.projzes.itl.infrastructure.persistence.SlaRepository;
import pl.edu.uj.projzes.itl.infrastructure.persistence.SlaViolationRepository;

import java.time.Clock;
import java.util.List;

@Service
public class SlaService {

    private final IncidentRepository incidentRepository;
    private final SlaRepository slaRepository;
    private final SlaViolationRepository violationRepository;
    private final Clock clock;

    public SlaService(IncidentRepository incidentRepository,
                      SlaRepository slaRepository,
                      SlaViolationRepository violationRepository,
                      Clock clock) {
        this.incidentRepository = incidentRepository;
        this.slaRepository = slaRepository;
        this.violationRepository = violationRepository;
        this.clock = clock;
    }

    public void checkSlaBreaches() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<SlaViolation> getViolationsForIncident(String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }
}
