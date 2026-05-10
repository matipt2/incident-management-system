package pl.edu.uj.projzes.itl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.application.IncidentService
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyIncidentAndManagementControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    IncidentService incidentService

    def "reporter widzi tylko swoje incydenty przez /api/my/incidents"() {
        given:
        def reporterToken = registerAndLogin("reporterA", "reporterA@example.com", "password123", "REPORTER")
        registerAndLogin("reporterB", "reporterB@example.com", "password123", "REPORTER")

        incidentService.reportIncident("A1", "opis", "reporterA", "API", "P-1")
        incidentService.reportIncident("B1", "opis", "reporterB", "API", "P-2")

        when:
        def result = mockMvc.perform(get("/api/my/incidents")
                .header("Authorization", "Bearer ${reporterToken}"))
                .andReturn()

        then:
        result.response.status == 200
        def json = objectMapper.readValue(result.response.contentAsString, List)
        json.size() == 1
        json[0].reportedBy == "reporterA"
    }

    def "agent widzi tylko incydenty przypisane do niego"() {
        given:
        registerAndLogin("manager1", "manager1@example.com", "password123", "MANAGER")
        def agentToken = registerAndLogin("agentX", "agentX@example.com", "password123", "AGENT")
        registerAndLogin("agentY", "agentY@example.com", "password123", "AGENT")

        def a = incidentService.reportIncident("INC-A", "opis", "rep", "API", "P-1")
        def b = incidentService.reportIncident("INC-B", "opis", "rep", "API", "P-1")
        incidentService.assignToAgent(a.id, "agentX")
        incidentService.assignToAgent(b.id, "agentY")

        when:
        def result = mockMvc.perform(get("/api/my/incidents")
                .header("Authorization", "Bearer ${agentToken}"))
                .andReturn()

        then:
        result.response.status == 200
        def json = objectMapper.readValue(result.response.contentAsString, List)
        json.size() == 1
        json[0].assignedTo == "agentX"
    }

    def "manager widzi wszystkie incydenty"() {
        given:
        def managerToken = registerAndLogin("manager2", "manager2@example.com", "password123", "MANAGER")
        incidentService.reportIncident("INC-1", "opis", "rep1", "API", "P-1")
        incidentService.reportIncident("INC-2", "opis", "rep2", "API", "P-2")

        when:
        def result = mockMvc.perform(get("/api/my/incidents")
                .header("Authorization", "Bearer ${managerToken}"))
                .andReturn()

        then:
        result.response.status == 200
        def json = objectMapper.readValue(result.response.contentAsString, List)
        json.size() >= 2
    }

    def "manager może przypisać incydent przez /api/management/incidents/{id}/assignment"() {
        given:
        def managerToken = registerAndLogin("manager3", "manager3@example.com", "password123", "MANAGER")
        registerAndLogin("agentAssign", "agentAssign@example.com", "password123", "AGENT")
        def incident = incidentService.reportIncident("INC-ASSIGN", "opis", "rep", "API", "P-1")

        when:
        def result = mockMvc.perform(post("/api/management/incidents/${incident.id}/assignment")
                .header("Authorization", "Bearer ${managerToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"agentId":"agentAssign"}'))
                .andReturn()

        then:
        result.response.status == 200
        def json = objectMapper.readValue(result.response.contentAsString, Map)
        json.assignedTo == "agentAssign"
        json.status == "IN_PROGRESS"
    }

    def "agent nie może użyć management assignment endpoint"() {
        given:
        def agentToken = registerAndLogin("agentNoManage", "agentNoManage@example.com", "password123", "AGENT")
        registerAndLogin("agentTarget", "agentTarget@example.com", "password123", "AGENT")
        def incident = incidentService.reportIncident("INC-NO", "opis", "rep", "API", "P-1")

        when:
        def result = mockMvc.perform(post("/api/management/incidents/${incident.id}/assignment")
                .header("Authorization", "Bearer ${agentToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"agentId":"agentTarget"}'))
                .andReturn()

        then:
        result.response.status == 403
    }

    private String registerAndLogin(String username, String email, String password, String role) {
        def registerBody = [username: username, email: email, password: password, role: role]
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody)))
                .andReturn()

        def loginBody = [username: username, password: password]
        def loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginBody)))
                .andReturn()

        def json = objectMapper.readValue(loginResult.response.contentAsString, Map)
        return json.token as String
    }
}
