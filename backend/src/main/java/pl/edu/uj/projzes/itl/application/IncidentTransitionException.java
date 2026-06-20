package pl.edu.uj.projzes.itl.application;

import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus;

public class IncidentTransitionException extends RuntimeException {

    public IncidentTransitionException(String action, IncidentStatus status) {
        super("Cannot " + action + " incident in status " + status);
    }
}
