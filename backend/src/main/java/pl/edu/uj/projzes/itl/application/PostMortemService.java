package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemReport;
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemStatus;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;
import pl.edu.uj.projzes.itl.infrastructure.persistence.PostMortemRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class PostMortemService {

    private final IncidentRepository incidentRepository;
    private final PostMortemRepository postMortemRepository;
    private final Clock clock;

    public PostMortemService(IncidentRepository incidentRepository,
                             PostMortemRepository postMortemRepository,
                             Clock clock) {
        this.incidentRepository = incidentRepository;
        this.postMortemRepository = postMortemRepository;
        this.clock = clock;
    }

    @Transactional
    public PostMortemReport create(String incidentId, String author) {
        ensureIncidentExists(incidentId);

        return postMortemRepository.findByIncidentId(incidentId)
                .orElseGet(() -> {
                    Instant now = clock.instant();
                    PostMortemReport report = new PostMortemReport();
                    report.setId(UUID.randomUUID().toString());
                    report.setIncidentId(incidentId);
                    report.setAuthor(author);
                    report.setStatus(PostMortemStatus.DRAFT);
                    report.setCreatedAt(now);
                    report.setUpdatedAt(now);
                    return postMortemRepository.save(report);
                });
    }

    @Transactional
    public PostMortemReport update(String incidentId, String rootCause, String timeline,
                                   String impact, String actionItems, String author) {
        PostMortemReport report = getByIncidentId(incidentId);
        report.setRootCause(rootCause);
        report.setTimeline(timeline);
        report.setImpact(impact);
        report.setActionItems(actionItems);
        report.setAuthor(author);
        report.setUpdatedAt(clock.instant());
        report.setStatus(PostMortemStatus.DRAFT);
        return postMortemRepository.save(report);
    }

    @Transactional
    public PostMortemReport approve(String incidentId, String approver) {
        PostMortemReport report = getByIncidentId(incidentId);
        if (isBlank(report.getRootCause())
                || isBlank(report.getTimeline())
                || isBlank(report.getImpact())
                || isBlank(report.getActionItems())) {
            throw new IllegalStateException("Raport post-mortem wymaga uzupełnienia przed zatwierdzeniem");
        }

        report.setAuthor(approver);
        report.setStatus(PostMortemStatus.APPROVED);
        report.setUpdatedAt(clock.instant());
        return postMortemRepository.save(report);
    }

    @Transactional(readOnly = true)
    public PostMortemReport getByIncidentId(String incidentId) {
        return postMortemRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new PostMortemNotFoundException(incidentId));
    }

    private void ensureIncidentExists(String incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new IncidentNotFoundException(incidentId);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
