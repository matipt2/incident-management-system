package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import pl.edu.uj.projzes.itl.infrastructure.llm.ClassificationResult;
import pl.edu.uj.projzes.itl.infrastructure.llm.LlmClassificationPort;

@Service
public class ClassificationService {

    private final LlmClassificationPort llmPort;

    public ClassificationService(LlmClassificationPort llmPort) {
        this.llmPort = llmPort;
    }

    /** Klasyfikuje incydent asynchronicznie i zapisuje wynik jako zdarzenie w historii. */
    public void classifyAsync(String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Zwraca rekomendację LLM bez zapisywania — tryb podglądu. */
    public ClassificationResult preview(String title, String description) {
        throw new UnsupportedOperationException("TODO");
    }
}
