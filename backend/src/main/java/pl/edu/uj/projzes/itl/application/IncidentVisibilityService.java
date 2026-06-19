package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentEvent;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.domain.user.CurrentUser;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class IncidentVisibilityService {

    private final IncidentRepository incidentRepository;

    public IncidentVisibilityService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public List<Incident> getVisibleIncidents(CurrentUser user,
                                              IncidentStatus status,
                                              IncidentPriority priority,
                                              IncidentCategory category,
                                              String projectId,
                                              String search) {
        List<Incident> base = switch (user.role()) {
            case REPORTER -> incidentRepository.findByReportedBy(user.username());
            case AGENT -> incidentRepository.findByAssignedTo(user.username());
            case VIEWER, MANAGER -> incidentRepository.findAll();
        };

        return base.stream()
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> priority == null || i.getPriority() == priority)
                .filter(i -> category == null || i.getCategory() == category)
                .filter(i -> projectId == null || projectId.isBlank() || projectId.equals(i.getProjectId()))
                .filter(i -> matchesSearch(i, search))
                .sorted(Comparator.comparing(Incident::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Incident getVisibleIncident(String incidentId, CurrentUser user) {
        return switch (user.role()) {
            case REPORTER -> incidentRepository.findByIdAndReportedBy(incidentId, user.username())
                    .orElseThrow(() -> new IncidentNotFoundException(incidentId));
            case AGENT -> incidentRepository.findByIdAndAssignedTo(incidentId, user.username())
                    .orElseThrow(() -> new IncidentNotFoundException(incidentId));
            case VIEWER, MANAGER -> incidentRepository.findById(incidentId)
                    .orElseThrow(() -> new IncidentNotFoundException(incidentId));
        };
    }

    @Transactional(readOnly = true)
    public List<IncidentEvent> getVisibleIncidentHistory(String incidentId, CurrentUser user) {
        Incident incident = getVisibleIncident(incidentId, user);
        return incident.getEvents();
    }

    private boolean matchesSearch(Incident incident, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        String query = search.toLowerCase(Locale.ROOT);
        return contains(incident.getId(), query)
                || contains(incident.getTitle(), query)
                || contains(incident.getDescription(), query)
                || contains(incident.getReportedBy(), query)
                || contains(incident.getProjectId(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
