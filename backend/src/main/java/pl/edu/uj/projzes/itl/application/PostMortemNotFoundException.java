package pl.edu.uj.projzes.itl.application;

public class PostMortemNotFoundException extends RuntimeException {

    public PostMortemNotFoundException(String incidentId) {
        super("Raport post-mortem nie istnieje dla incydentu: " + incidentId);
    }
}
