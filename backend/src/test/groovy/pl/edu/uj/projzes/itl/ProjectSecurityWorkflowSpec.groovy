package pl.edu.uj.projzes.itl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import pl.edu.uj.projzes.itl.api.dto.LoginRequest
import pl.edu.uj.projzes.itl.api.dto.RegisterRequest
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.application.IncidentTransitionException
import pl.edu.uj.projzes.itl.application.ProjectInactiveException
import pl.edu.uj.projzes.itl.application.ProjectNotFoundException
import pl.edu.uj.projzes.itl.application.ProjectService
import pl.edu.uj.projzes.itl.application.SlaService
import pl.edu.uj.projzes.itl.application.UserRoleChangeException
import pl.edu.uj.projzes.itl.application.UserService
import pl.edu.uj.projzes.itl.domain.incident.IncidentPriority
import pl.edu.uj.projzes.itl.domain.incident.IncidentStatus
import pl.edu.uj.projzes.itl.domain.user.UserRole
import pl.edu.uj.projzes.itl.infrastructure.persistence.UserRepository
import spock.lang.Specification

import java.time.Duration
import java.util.UUID

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectSecurityWorkflowSpec extends Specification {

    @Autowired MockMvc mockMvc
    @Autowired ObjectMapper objectMapper
    @Autowired ProjectService projectService
    @Autowired IncidentService incidentService
    @Autowired SlaService slaService
    @Autowired UserService userService
    @Autowired UserRepository userRepository

    def "manager creates and deactivates a project while reporters only list active projects"() {
        given:
        def managerToken = registerWithRole(unique("manager"), UserRole.MANAGER)
        def reporterToken = registerWithRole(unique("reporter"), UserRole.REPORTER)
        def key = "OPS-${UUID.randomUUID().toString().take(6).toUpperCase()}"

        when:
        def createResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer ${managerToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString([
                        key: key.toString(), name: "Operations", description: "Core operations"
                ]))).andReturn()
        def deactivateResult = mockMvc.perform(patch("/api/projects/${key}/status")
                .header("Authorization", "Bearer ${managerToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"active":false}')).andReturn()
        def reporterList = mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer ${reporterToken}")).andReturn()
        def managerList = mockMvc.perform(get("/api/projects")
                .param("includeInactive", "true")
                .header("Authorization", "Bearer ${managerToken}")).andReturn()

        then:
        createResult.response.status == 201
        deactivateResult.response.status == 200
        !objectMapper.readValue(reporterList.response.contentAsString, List)*.key.contains(key.toString())
        objectMapper.readValue(managerList.response.contentAsString, List)*.key.contains(key.toString())
    }

    def "unknown and inactive projects are rejected by incidents and SLA policies"() {
        given:
        def inactiveKey = "INACTIVE-${UUID.randomUUID().toString().take(6).toUpperCase()}"
        projectService.create(inactiveKey, "Inactive", null)
        projectService.setActive(inactiveKey, false)

        when:
        incidentService.reportIncident("Unknown", "Description", "reporter", "API", "MISSING-PROJECT")

        then:
        thrown(ProjectNotFoundException)

        when:
        incidentService.reportIncident("Inactive", "Description", "reporter", "API", inactiveKey)

        then:
        thrown(ProjectInactiveException)

        when:
        slaService.createPolicy(
                inactiveKey, IncidentPriority.HIGH,
                Duration.ofMinutes(30), Duration.ofHours(4), 100.00G)

        then:
        thrown(ProjectInactiveException)
    }

    def "public registration always creates reporter and role management protects managers"() {
        given:
        def publicUsername = unique("public")
        def registerBody = [
                username: publicUsername,
                email: "${publicUsername}@example.com".toString(),
                password: "password123",
                role: "MANAGER"
        ]

        when:
        def registration = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody))).andReturn()

        then:
        registration.response.status == 201
        objectMapper.readValue(registration.response.contentAsString, Map).role == "REPORTER"

        when:
        def onlyManager = userService.createBootstrapManager(
                unique("bootstrap"), unique("bootstrap") + "@example.com", "password123")
        userService.updateRole(onlyManager.id, UserRole.AGENT, "another-manager")

        then:
        thrown(UserRoleChangeException)

        when:
        userService.updateRole(onlyManager.id, UserRole.AGENT, onlyManager.username)

        then:
        thrown(UserRoleChangeException)
    }

    def "viewer reads all incidents but cannot report or mutate them"() {
        given:
        def viewerToken = registerWithRole(unique("viewer"), UserRole.VIEWER)
        incidentService.reportIncident("Visible A", "Description", "one", "API", "PROJ-1")
        incidentService.reportIncident("Visible B", "Description", "two", "API", "PROJ-1")

        when:
        def listResult = mockMvc.perform(get("/api/incidents")
                .header("Authorization", "Bearer ${viewerToken}")).andReturn()
        def reportResult = mockMvc.perform(post("/api/incidents")
                .header("Authorization", "Bearer ${viewerToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"title":"Denied","description":"Denied","channel":"API","projectId":"PROJ-1"}'))
                .andReturn()

        then:
        listResult.response.status == 200
        objectMapper.readValue(listResult.response.contentAsString, List).size() >= 2
        reportResult.response.status == 403
    }

    def "assigned agent resolves an incident while another agent is denied"() {
        given:
        def assignedUsername = unique("assigned")
        def otherUsername = unique("other")
        def assignedToken = registerWithRole(assignedUsername, UserRole.AGENT)
        def otherToken = registerWithRole(otherUsername, UserRole.AGENT)
        def incident = incidentService.reportIncident("Workflow", "Description", "reporter", "API", "PROJ-1")
        incidentService.assignToAgent(incident.id, assignedUsername, "manager")

        when:
        def denied = mockMvc.perform(post("/api/incidents/${incident.id}/resolve")
                .param("resolution", "Not mine")
                .header("Authorization", "Bearer ${otherToken}")).andReturn()
        def resolved = mockMvc.perform(post("/api/incidents/${incident.id}/resolve")
                .param("resolution", "Fixed")
                .header("Authorization", "Bearer ${assignedToken}")).andReturn()

        then:
        denied.response.status == 404
        resolved.response.status == 200
        objectMapper.readValue(resolved.response.contentAsString, Map).status == "RESOLVED"
    }

    def "invalid lifecycle transition returns conflict and does not append history"() {
        given:
        def incident = incidentService.reportIncident("Transition", "Description", "reporter", "API", "PROJ-1")
        def eventsBefore = incidentService.getEvents(incident.id, null, null, null).size()

        when:
        incidentService.resolve(incident.id, "Too early", "agent")

        then:
        thrown(IncidentTransitionException)
        incidentService.getById(incident.id).status == IncidentStatus.NEW
        incidentService.getEvents(incident.id, null, null, null).size() == eventsBefore
    }

    private String registerWithRole(String username, UserRole role) {
        userService.register(new RegisterRequest(
                username, "${username}@example.com".toString(), "password123"))
        def user = userRepository.findByUsername(username).orElseThrow()
        user.role = role
        userRepository.saveAndFlush(user)
        return userService.login(new LoginRequest(
                username, "password123")).token()
    }

    private String unique(String prefix) {
        return "${prefix}-${UUID.randomUUID().toString().take(8)}"
    }
}
