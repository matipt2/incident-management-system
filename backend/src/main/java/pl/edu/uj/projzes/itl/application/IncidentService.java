package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public Incident reportIncident(String title, String description,
                                   String reportedBy, String channel, String projectId) {
        throw new UnsupportedOperationException("TODO");
    }

    public Incident assignToAgent(String incidentId, String agentId) {
        throw new UnsupportedOperationException("TODO");
    }

    public Incident applyClassification(String incidentId, IncidentPriority priority,
                                        IncidentCategory category, String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }

    public Incident escalate(String incidentId, String reason, String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }

    public Incident resolve(String incidentId, String resolution, String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }

    public Incident close(String incidentId, String performedBy) {
        throw new UnsupportedOperationException("TODO");
    }

    public Incident getById(String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Incident> getAll() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<Incident> getByStatus(IncidentStatus status) {
        throw new UnsupportedOperationException("TODO");
    }
}
