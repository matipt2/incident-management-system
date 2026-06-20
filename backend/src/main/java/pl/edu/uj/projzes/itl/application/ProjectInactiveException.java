package pl.edu.uj.projzes.itl.application;

public class ProjectInactiveException extends RuntimeException {

    public ProjectInactiveException(String key) {
        super("Project is inactive: " + key);
    }
}
