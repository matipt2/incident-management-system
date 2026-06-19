package pl.edu.uj.projzes.itl.application;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String key) {
        super("Project not found: " + key);
    }
}
