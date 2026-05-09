package pl.edu.uj.projzes.itl.application;

public class SlaPolicyNotFoundException extends RuntimeException {

    public SlaPolicyNotFoundException(Long id) {
        super("Polityka SLA nie istnieje: " + id);
    }
}
