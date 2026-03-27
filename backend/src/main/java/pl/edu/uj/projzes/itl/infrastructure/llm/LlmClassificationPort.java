package pl.edu.uj.projzes.itl.infrastructure.llm;

public interface LlmClassificationPort {

    ClassificationResult classify(String title, String description);
}
