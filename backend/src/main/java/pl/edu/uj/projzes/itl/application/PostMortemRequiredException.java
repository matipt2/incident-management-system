package pl.edu.uj.projzes.itl.application;

public class PostMortemRequiredException extends RuntimeException {

    public PostMortemRequiredException(String incidentId) {
        super("Incydent krytyczny wymaga zatwierdzonego raportu post-mortem przed zamknięciem: " + incidentId);
    }
}
