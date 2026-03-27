package pl.edu.uj.projzes.itl.infrastructure.llm;

import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory;
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority;

public record ClassificationResult(
        IncidentPriority suggestedPriority,
        IncidentCategory suggestedCategory,
        String rationale
) {}
