package pl.edu.uj.projzes.itl.application;

import org.springframework.stereotype.Service;
import pl.edu.uj.projzes.itl.domain.postmortem.PostMortemReport;

@Service
public class PostMortemService {

    public PostMortemReport create(String incidentId, String author) {
        throw new UnsupportedOperationException("TODO");
    }

    public PostMortemReport update(String incidentId, String rootCause, String timeline,
                                   String impact, String actionItems, String author) {
        throw new UnsupportedOperationException("TODO");
    }

    public PostMortemReport approve(String incidentId, String approver) {
        throw new UnsupportedOperationException("TODO");
    }

    public PostMortemReport getByIncidentId(String incidentId) {
        throw new UnsupportedOperationException("TODO");
    }
}
