package pl.edu.uj.projzes.itl.infrastructure.llm;

import org.springframework.stereotype.Component;
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;

/**
 * Tymczasowy stub — zwraca stałą odpowiedź.
 * Należy zastąpić prawdziwą integracją z LLM.
 */
@Component
public class StubLlmClassificationAdapter implements LlmClassificationPort {

    @Override
    public ClassificationResult classify(String title, String description) {
        // TODO: zastąpić wywołaniem do modelu językowego (np. Claude, OpenAI)
        return new ClassificationResult(
                IncidentPriority.MEDIUM,
                IncidentCategory.OTHER,
                "Klasyfikacja tymczasowa - stub LLM"
        );
    }
}
