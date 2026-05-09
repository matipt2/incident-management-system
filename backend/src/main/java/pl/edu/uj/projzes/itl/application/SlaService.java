package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.uj.projzes.itl.domain.incident.Incident;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;
import pl.edu.uj.projzes.itl.domain.sla.SlaPolicy;
import pl.edu.uj.projzes.itl.domain.sla.SlaViolation;
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository;
import pl.edu.uj.projzes.itl.infrastructure.persistence.SlaRepository;
import pl.edu.uj.projzes.itl.infrastructure.persistence.SlaViolationRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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

    @Transactional
    public void checkSlaBreaches() {
        Instant now = clock.instant();
        incidentRepository.findAll().stream()
                .filter(this::isOpen)
                .filter(incident -> incident.getPriority() != null)
                .filter(incident -> incident.getProjectId() != null)
                .forEach(incident -> slaRepository
                        .findFirstByProjectIdAndPriorityOrderByIdAsc(incident.getProjectId(), incident.getPriority())
                        .ifPresent(policy -> detectViolations(incident, policy, now)));
    }

    @Transactional(readOnly = true)
    public List<SlaViolation> getViolationsForIncident(String incidentId) {
        return violationRepository.findByIncidentId(incidentId);
    }

    @Transactional(readOnly = true)
    public List<SlaViolation> getViolations(String projectId) {
        return projectId == null || projectId.isBlank()
                ? violationRepository.findAll()
                : violationRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<SlaPolicy> getPolicies() {
        return slaRepository.findAll();
    }

    @Transactional
    public SlaPolicy createPolicy(String projectId, IncidentPriority priority,
                                  Duration responseTime, Duration resolutionTime,
                                  BigDecimal penaltyAmount) {
        SlaPolicy policy = slaRepository.findFirstByProjectIdAndPriorityOrderByIdAsc(projectId, priority)
                .orElseGet(SlaPolicy::new);
        policy.setProjectId(projectId);
        policy.setPriority(priority);
        policy.setResponseTime(responseTime);
        policy.setResolutionTime(resolutionTime);
        policy.setPenaltyAmount(penaltyAmount);
        return slaRepository.save(policy);
    }

    @Transactional
    public SlaPolicy updatePolicy(Long id, String projectId, IncidentPriority priority,
                                  Duration responseTime, Duration resolutionTime,
                                  BigDecimal penaltyAmount) {
        SlaPolicy policy = slaRepository.findById(id)
                .orElseThrow(() -> new SlaPolicyNotFoundException(id));
        policy.setProjectId(projectId);
        policy.setPriority(priority);
        policy.setResponseTime(responseTime);
        policy.setResolutionTime(resolutionTime);
        policy.setPenaltyAmount(penaltyAmount);
        return slaRepository.save(policy);
    }

    @Transactional
    public SlaViolation applyPenalty(Long violationId) {
        SlaViolation violation = violationRepository.findById(violationId)
                .orElseThrow(() -> new SlaViolationNotFoundException(violationId));
        violation.setPenaltyApplied(true);
        return violationRepository.save(violation);
    }

    private void detectViolations(Incident incident, SlaPolicy policy, Instant now) {
        if (deadlinePassed(incident.getCreatedAt(), policy.getResponseTime(), now)
                && incident.getAssignedTo() == null) {
            createViolationIfMissing(incident, policy, SlaViolation.ViolationType.RESPONSE_TIME_EXCEEDED, now);
        }

        if (deadlinePassed(incident.getCreatedAt(), policy.getResolutionTime(), now)) {
            createViolationIfMissing(incident, policy, SlaViolation.ViolationType.RESOLUTION_TIME_EXCEEDED, now);
        }
    }

    private boolean deadlinePassed(Instant startedAt, Duration allowedTime, Instant now) {
        return startedAt != null
                && allowedTime != null
                && now.isAfter(startedAt.plus(allowedTime));
    }

    private void createViolationIfMissing(Incident incident, SlaPolicy policy,
                                          SlaViolation.ViolationType violationType,
                                          Instant detectedAt) {
        if (violationRepository.existsByIncidentIdAndViolationType(incident.getId(), violationType)) {
            return;
        }

        SlaViolation violation = new SlaViolation();
        violation.setIncidentId(incident.getId());
        violation.setProjectId(incident.getProjectId());
        violation.setViolationType(violationType);
        violation.setDetectedAt(detectedAt);
        violation.setPenalty(policy.getPenaltyAmount());
        violation.setPenaltyApplied(false);
        violationRepository.save(violation);
    }

    private boolean isOpen(Incident incident) {
        return incident.getStatus() != IncidentStatus.RESOLVED
                && incident.getStatus() != IncidentStatus.CLOSED;
    }
}
