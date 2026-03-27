package pl.edu.uj.projzes.itl.application;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(String id) {
        super("Incydent nie istnieje: " + id);
    }
}
