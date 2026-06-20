package pl.edu.uj.projzes.itl.application;

public class ProjectAlreadyExistsException extends RuntimeException {

    public ProjectAlreadyExistsException(String key) {
        super("Project already exists: " + key);
    }
}
