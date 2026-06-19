package pl.edu.uj.projzes.itl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.application.SlaService
import pl.edu.uj.projzes.itl.domain.incident.IncidentCategory
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority
import pl.edu.uj.projzes.itl.domain.user.User
import pl.edu.uj.projzes.itl.domain.user.UserRole
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository
import pl.edu.uj.projzes.itl.infrastructure.persistence.UserRepository
import pl.edu.uj.projzes.itl.infrastructure.security.JwtService
import spock.lang.Specification

import java.time.Duration
import java.time.Instant
import java.util.UUID

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BackendControllerFlowSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    IncidentService incidentService

    @Autowired
    IncidentRepository incidentRepository

    @Autowired
    SlaService slaService

    @Autowired
    JwtService jwtService

    @Autowired
    UserRepository userRepository

    def "niepoprawna polityka SLA z realnym tokenem JWT zwraca 400"() {
        given:
        def user = new User("jwt_validation", "jwt_validation@example.com", "hash", UserRole.MANAGER)
        user.id = UUID.randomUUID()
        userRepository.saveAndFlush(user)
        def token = jwtService.generateToken(user)
        def body = [
                projectId             : "",
                priority              : "HIGH",
                responseTimeMinutes   : 0,
                resolutionTimeMinutes : 60,
                penaltyAmount         : 100.00
        ]

        when:
        def result = mockMvc.perform(post("/api/sla/policies")
                .header("Authorization", "Bearer ${token}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andReturn()

        then:
        result.response.status == 400
    }

    @WithMockUser(username = "manager", authorities = ["INCIDENT_READ"])
    def "historia incydentu zwraca zdarzenia i filtruje po typie"() {
        given:
        def incident = incidentService.reportIncident("Awaria historii", "Opis", "user", "API", "PROJ-HIST")
        incidentService.assignToAgent(incident.id, "agent1")

        when:
        def allResult = mockMvc.perform(get("/api/incidents/${incident.id}/events")).andReturn()
        def filteredResult = mockMvc.perform(get("/api/incidents/${incident.id}/events")
                .param("eventType", "ASSIGNED"))
                .andReturn()

        then:
        allResult.response.status == 200
        def allEvents = objectMapper.readValue(allResult.response.contentAsString, List)
        allEvents.size() >= 2

        filteredResult.response.status == 200
        def filteredEvents = objectMapper.readValue(filteredResult.response.contentAsString, List)
        filteredEvents.size() == 1
        filteredEvents[0].eventType == "ASSIGNED"
    }

    @WithMockUser(username = "manager", authorities = ["INCIDENT_READ"])
    def "historia incydentu filtruje zdarzenia po zakresie dat"() {
        given:
        def incident = incidentService.reportIncident("Awaria dat", "Opis", "user", "API", "PROJ-HIST-DATE")
        incidentService.assignToAgent(incident.id, "agent1")

        when:
        def inRangeResult = mockMvc.perform(get("/api/incidents/${incident.id}/events")
                .param("from", Instant.now().minus(Duration.ofMinutes(1)).toString())
                .param("to", Instant.now().plus(Duration.ofMinutes(1)).toString()))
                .andReturn()
        def futureRangeResult = mockMvc.perform(get("/api/incidents/${incident.id}/events")
                .param("from", Instant.now().plus(Duration.ofMinutes(1)).toString()))
                .andReturn()

        then:
        inRangeResult.response.status == 200
        def inRangeEvents = objectMapper.readValue(inRangeResult.response.contentAsString, List)
        inRangeEvents.size() >= 2

        futureRangeResult.response.status == 200
        def futureRangeEvents = objectMapper.readValue(futureRangeResult.response.contentAsString, List)
        futureRangeEvents.isEmpty()
    }

    @WithMockUser(username = "manager", authorities = ["SLA_READ", "SLA_WRITE"])
    def "endpointy SLA pozwalają utworzyć politykę i uruchomić sprawdzenie naruszeń"() {
        given:
        def projectId = "PROJ-CTRL-SLA"
        def policy = [
                projectId             : projectId,
                priority              : "CRITICAL",
                responseTimeMinutes   : 1,
                resolutionTimeMinutes : 1,
                penaltyAmount         : 750.00
        ]
        def incident = incidentService.reportIncident("Stare zgłoszenie", "Opis", "user", "API", projectId)
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.APPLICATION, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofMinutes(10))

        when:
        def createPolicyResult = mockMvc.perform(post("/api/sla/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policy)))
                .andReturn()
        def checkResult = mockMvc.perform(post("/api/sla/check")).andReturn()

        then:
        createPolicyResult.response.status == 201
        def createdPolicy = objectMapper.readValue(createPolicyResult.response.contentAsString, Map)
        createdPolicy.projectId == projectId
        createdPolicy.priority == "CRITICAL"

        checkResult.response.status == 200
        def violations = objectMapper.readValue(checkResult.response.contentAsString, List)
        violations.any { it.incidentId == incident.id && it.violationType == "RESOLUTION_TIME_EXCEEDED" }
    }

    @WithMockUser(username = "manager", authorities = ["SLA_READ", "SLA_WRITE"])
    def "endpointy SLA pozwalają listować i aktualizować polityki"() {
        given:
        def projectId = "PROJ-POLICY-CTRL"
        def createBody = [
                projectId             : projectId,
                priority              : "HIGH",
                responseTimeMinutes   : 10,
                resolutionTimeMinutes : 60,
                penaltyAmount         : 300.00
        ]
        def updateBody = [
                projectId             : projectId,
                priority              : "HIGH",
                responseTimeMinutes   : 20,
                resolutionTimeMinutes : 120,
                penaltyAmount         : 450.00
        ]
        def createResult = mockMvc.perform(post("/api/sla/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBody)))
                .andReturn()
        def createdPolicy = objectMapper.readValue(createResult.response.contentAsString, Map)

        when:
        def listResult = mockMvc.perform(get("/api/sla/policies")).andReturn()
        def updateResult = mockMvc.perform(put("/api/sla/policies/${createdPolicy.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBody)))
                .andReturn()

        then:
        listResult.response.status == 200
        def policies = objectMapper.readValue(listResult.response.contentAsString, List)
        policies.any { it.projectId == projectId && it.priority == "HIGH" }

        updateResult.response.status == 200
        def updatedPolicy = objectMapper.readValue(updateResult.response.contentAsString, Map)
        updatedPolicy.responseTimeMinutes == 20
        updatedPolicy.resolutionTimeMinutes == 120
    }

    @WithMockUser(username = "manager", authorities = ["SLA_WRITE"])
    def "aktualizacja nieistniejącej polityki SLA zwraca 404"() {
        given:
        def body = [
                projectId             : "PROJ-MISSING-POLICY",
                priority              : "LOW",
                responseTimeMinutes   : 30,
                resolutionTimeMinutes : 240,
                penaltyAmount         : 100.00
        ]

        when:
        def result = mockMvc.perform(put("/api/sla/policies/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andReturn()

        then:
        result.response.status == 404
    }

    @WithMockUser(username = "manager", authorities = ["SLA_WRITE"])
    def "niepoprawna polityka SLA zwraca 400"() {
        given:
        def body = [
                projectId             : "",
                priority              : "HIGH",
                responseTimeMinutes   : 0,
                resolutionTimeMinutes : 60,
                penaltyAmount         : 100.00
        ]

        when:
        def result = mockMvc.perform(post("/api/sla/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andReturn()

        then:
        result.response.status == 400
    }

    @WithMockUser(username = "manager", authorities = ["SLA_READ", "SLA_WRITE"])
    def "endpointy SLA pozwalają pobrać naruszenia incydentu i naliczyć karę"() {
        given:
        def projectId = "PROJ-VIOLATION-CTRL"
        slaService.createPolicy(projectId, IncidentPriority.CRITICAL,
                Duration.ofMinutes(1), Duration.ofMinutes(1), 900.00G)
        def incident = incidentService.reportIncident("Naruszenie", "Opis", "user", "API", projectId)
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.NETWORK, "agent1")
        moveCreatedAtToPast(incident.id, Duration.ofMinutes(10))
        slaService.checkSlaBreaches()

        when:
        def listResult = mockMvc.perform(get("/api/sla/violations/${incident.id}")).andReturn()
        def violations = objectMapper.readValue(listResult.response.contentAsString, List)
        def violationId = violations.find { it.violationType == "RESOLUTION_TIME_EXCEEDED" }.id
        def applyPenaltyResult = mockMvc.perform(post("/api/sla/violations/${violationId}/apply-penalty")).andReturn()

        then:
        listResult.response.status == 200
        violations.any { it.incidentId == incident.id }

        applyPenaltyResult.response.status == 200
        def updatedViolation = objectMapper.readValue(applyPenaltyResult.response.contentAsString, Map)
        updatedViolation.penaltyApplied == true
    }

    @WithMockUser(username = "manager", authorities = ["POSTMORTEM_READ", "POSTMORTEM_WRITE", "POSTMORTEM_APPROVE"])
    def "endpointy post-mortem obsługują utworzenie aktualizację i zatwierdzenie raportu"() {
        given:
        def incident = incidentService.reportIncident("Awaria PM", "Opis", "user", "API", "PROJ-PM")
        def update = [
                rootCause  : "Root cause",
                timeline   : "Timeline",
                impact     : "Impact",
                actionItems: "Action items"
        ]

        when:
        def createResult = mockMvc.perform(post("/api/incidents/${incident.id}/post-mortem")).andReturn()
        def updateResult = mockMvc.perform(put("/api/incidents/${incident.id}/post-mortem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andReturn()
        def approveResult = mockMvc.perform(post("/api/incidents/${incident.id}/post-mortem/approve")).andReturn()

        then:
        createResult.response.status == 201

        updateResult.response.status == 200
        def updatedReport = objectMapper.readValue(updateResult.response.contentAsString, Map)
        updatedReport.rootCause == "Root cause"
        updatedReport.status == "DRAFT"

        approveResult.response.status == 200
        def approvedReport = objectMapper.readValue(approveResult.response.contentAsString, Map)
        approvedReport.status == "APPROVED"
    }

    @WithMockUser(username = "manager", authorities = ["POSTMORTEM_READ", "POSTMORTEM_WRITE", "POSTMORTEM_APPROVE"])
    def "endpointy post-mortem zwracają błędy dla brakujących i nieuzupełnionych raportów"() {
        given:
        def incident = incidentService.reportIncident("Awaria PM error", "Opis", "user", "API", "PROJ-PM-ERR")
        mockMvc.perform(post("/api/incidents/${incident.id}/post-mortem")).andReturn()

        when:
        def missingGetResult = mockMvc.perform(get("/api/incidents/missing-id/post-mortem")).andReturn()
        def incompleteApproveResult = mockMvc.perform(post("/api/incidents/${incident.id}/post-mortem/approve")).andReturn()
        def missingIncidentCreateResult = mockMvc.perform(post("/api/incidents/missing-id/post-mortem")).andReturn()

        then:
        missingGetResult.response.status == 404
        incompleteApproveResult.response.status == 400
        missingIncidentCreateResult.response.status == 404
    }

    @WithMockUser(username = "manager", authorities = ["POSTMORTEM_WRITE"])
    def "niepoprawny raport post-mortem zwraca 400"() {
        given:
        def incident = incidentService.reportIncident("Awaria PM validation", "Opis", "user", "API", "PROJ-PM-VALID")
        mockMvc.perform(post("/api/incidents/${incident.id}/post-mortem")).andReturn()
        def body = [
                rootCause  : "",
                timeline   : "Timeline",
                impact     : "Impact",
                actionItems: "Action items"
        ]

        when:
        def result = mockMvc.perform(put("/api/incidents/${incident.id}/post-mortem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andReturn()

        then:
        result.response.status == 400
    }

    @WithMockUser(username = "manager", authorities = ["INCIDENT_CLOSE"])
    def "zamknięcie krytycznego incydentu bez zatwierdzonego post-mortem zwraca konflikt"() {
        given:
        def incident = incidentService.reportIncident("Krytyczna awaria", "Opis", "user", "API", "PROJ-CRIT")
        incidentService.applyClassification(incident.id, IncidentPriority.CRITICAL, IncidentCategory.SECURITY, "agent1")
        incidentService.assignToAgent(incident.id, "agent1")
        incidentService.resolve(incident.id, "Naprawione", "agent1")

        when:
        def result = mockMvc.perform(post("/api/incidents/${incident.id}/close")).andReturn()

        then:
        result.response.status == 409
    }

    private void moveCreatedAtToPast(String incidentId, Duration age) {
        def incident = incidentRepository.findById(incidentId).orElseThrow()
        incident.createdAt = Instant.now().minus(age)
        incident.updatedAt = incident.createdAt
        incidentRepository.saveAndFlush(incident)
    }
}
