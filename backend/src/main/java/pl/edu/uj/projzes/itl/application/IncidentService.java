package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentEvent;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final Clock clock;

    public IncidentService(IncidentRepository incidentRepository, Clock clock) {
        this.incidentRepository = incidentRepository;
        this.clock = clock;
    }

    @Transactional
    public Incident reportIncident(String title, String description,
                                   String reportedBy, String channel, String projectId) {
        Instant now = clock.instant();

        Incident incident = new Incident();
        incident.setId(UUID.randomUUID().toString());
        incident.setTitle(title);
        incident.setDescription(description);
        incident.setReportedBy(reportedBy);
        incident.setChannel(channel);
        incident.setProjectId(projectId);
        incident.setStatus(IncidentStatus.NEW);
        incident.setCreatedAt(now);
        incident.setUpdatedAt(now);

        addEvent(incident, "INCIDENT_REPORTED",
                "Incydent zgłoszony przez " + reportedBy + " przez kanał " + channel,
                reportedBy, now);

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident assignToAgent(String incidentId, String agentId) {
        Instant now = clock.instant();
        Incident incident = getById(incidentId);

        incident.setAssignedTo(agentId);
        incident.setStatus(IncidentStatus.IN_PROGRESS);
        incident.setUpdatedAt(now);

        addEvent(incident, "ASSIGNED",
                "Incydent przypisany do agenta: " + agentId,
                agentId, now);

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident applyClassification(String incidentId, IncidentPriority priority,
                                        IncidentCategory category, String performedBy) {
        Instant now = clock.instant();
        Incident incident = getById(incidentId);

        incident.setPriority(priority);
        incident.setCategory(category);
        incident.setUpdatedAt(now);

        addEvent(incident, "CLASSIFICATION_APPLIED",
                "Klasyfikacja: priorytet=" + priority + ", kategoria=" + category,
                performedBy, now);

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident escalate(String incidentId, String reason, String performedBy) {
        Instant now = clock.instant();
        Incident incident = getById(incidentId);

        incident.setStatus(IncidentStatus.ESCALATED);
        incident.setUpdatedAt(now);

        addEvent(incident, "ESCALATED",
                "Eskalacja: " + reason,
                performedBy, now);

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident resolve(String incidentId, String resolution, String performedBy) {
        Instant now = clock.instant();
        Incident incident = getById(incidentId);

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(now);
        incident.setUpdatedAt(now);

        addEvent(incident, "RESOLVED",
                "Rozwiązanie: " + resolution,
                performedBy, now);

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident close(String incidentId, String performedBy) {
        Instant now = clock.instant();
        Incident incident = getById(incidentId);

        incident.setStatus(IncidentStatus.CLOSED);
        incident.setUpdatedAt(now);

        addEvent(incident, "CLOSED",
                "Incydent zamknięty",
                performedBy, now);

        return incidentRepository.save(incident);
    }

    @Transactional(readOnly = true)
    public Incident getById(String incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));
    }

    @Transactional(readOnly = true)
    public List<Incident> getAll() {
        return incidentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Incident> getByStatus(IncidentStatus status) {
        return incidentRepository.findByStatus(status);
    }

    private void addEvent(Incident incident, String eventType, String details,
                          String performedBy, Instant occurredAt) {
        IncidentEvent event = new IncidentEvent();
        event.setId(UUID.randomUUID().toString());
        event.setIncident(incident);
        event.setEventType(eventType);
        event.setDetails(details);
        event.setPerformedBy(performedBy);
        event.setOccurredAt(occurredAt);
        incident.getEvents().add(event);
    }
}
