package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentEvent;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemStatus;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;
import pl.edu.uj.projzes.itl.infrastructure.persistence.PostMortemRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final PostMortemRepository postMortemRepository;
    private final ProjectService projectService;
    private final Clock clock;

    public IncidentService(IncidentRepository incidentRepository,
                           PostMortemRepository postMortemRepository,
                           ProjectService projectService,
                           Clock clock) {
        this.incidentRepository = incidentRepository;
        this.postMortemRepository = postMortemRepository;
        this.projectService = projectService;
        this.clock = clock;
    }

    @Transactional
    public Incident reportIncident(String title, String description,
                                   String reportedBy, String channel, String projectId) {
        String validatedProjectKey = projectService.requireActive(projectId).getKey();
        Instant now = clock.instant();

        Incident incident = new Incident();
        incident.setId(UUID.randomUUID().toString());
        incident.setTitle(title);
        incident.setDescription(description);
        incident.setReportedBy(reportedBy);
        incident.setChannel(channel);
        incident.setProjectId(validatedProjectKey);
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
        return assignToAgent(incidentId, agentId, agentId);
    }

    @Transactional
    public Incident assignToAgent(String incidentId, String agentId, String performedBy) {
        Instant now = clock.instant();
        Incident incident = getById(incidentId);
        requireStatus(
                incident,
                "assign",
                IncidentStatus.NEW,
                IncidentStatus.IN_PROGRESS,
                IncidentStatus.ESCALATED
        );

        incident.setAssignedTo(agentId);
        if (incident.getStatus() == IncidentStatus.NEW) {
            incident.setStatus(IncidentStatus.IN_PROGRESS);
        }
        incident.setUpdatedAt(now);

        addEvent(incident, "ASSIGNED",
                "Incydent przypisany do agenta: " + agentId,
                performedBy, now);

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident applyClassification(String incidentId, IncidentPriority priority,
                                        IncidentCategory category, String performedBy) {
        Instant now = clock.instant();
        Incident incident = getById(incidentId);
        requireStatus(
                incident,
                "classify",
                IncidentStatus.NEW,
                IncidentStatus.IN_PROGRESS,
                IncidentStatus.ESCALATED
        );

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
        requireStatus(incident, "escalate", IncidentStatus.NEW, IncidentStatus.IN_PROGRESS);

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
        requireStatus(incident, "resolve", IncidentStatus.IN_PROGRESS, IncidentStatus.ESCALATED);

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
        requireStatus(incident, "close", IncidentStatus.RESOLVED);
        ensurePostMortemApprovedIfCritical(incident);

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

    @Transactional(readOnly = true)
    public List<IncidentEvent> getEvents(String incidentId, String eventType,
                                         Instant from, Instant to) {
        return getById(incidentId).getEvents().stream()
                .filter(event -> eventType == null || eventType.isBlank() || eventType.equals(event.getEventType()))
                .filter(event -> from == null || !event.getOccurredAt().isBefore(from))
                .filter(event -> to == null || !event.getOccurredAt().isAfter(to))
                .toList();
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

    private void ensurePostMortemApprovedIfCritical(Incident incident) {
        if (incident.getPriority() != IncidentPriority.CRITICAL) {
            return;
        }

        boolean approvedPostMortemExists = postMortemRepository.findByIncidentId(incident.getId())
                .map(report -> report.getStatus() == PostMortemStatus.APPROVED)
                .orElse(false);
        if (!approvedPostMortemExists) {
            throw new PostMortemRequiredException(incident.getId());
        }
    }

    private void requireStatus(Incident incident, String action, IncidentStatus... allowedStatuses) {
        for (IncidentStatus allowedStatus : allowedStatuses) {
            if (incident.getStatus() == allowedStatus) {
                return;
            }
        }
        throw new IncidentTransitionException(action, incident.getStatus());
    }
}
