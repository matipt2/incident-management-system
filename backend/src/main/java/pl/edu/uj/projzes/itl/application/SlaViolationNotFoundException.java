package pl.edu.uj.projzes.itl.application;

public class SlaViolationNotFoundException extends RuntimeException {

    public SlaViolationNotFoundException(Long id) {
        super("Naruszenie SLA nie istnieje: " + id);
    }
}
