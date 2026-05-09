package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EndpointSecuritySpec extends Specification {

    @Autowired
    MockMvc mockMvc

    // --- żądania bez tokenu ---

    def "niezalogowany użytkownik dostaje 401 na chronionym endpoincie"() {
        when:
        def result = mockMvc.perform(get("/api/incidents")).andReturn()

        then:
        result.response.status == 401
    }

    def "niezalogowany użytkownik dostaje 401 na endpoincie SLA"() {
        when:
        def result = mockMvc.perform(get("/api/sla/violations")).andReturn()

        then:
        result.response.status == 401
    }

    // --- endpointy publiczne ---

    def "endpoint rejestracji jest dostępny bez tokenu"() {
        when:
        def result = mockMvc.perform(
                post("/api/auth/register")
                        .contentType("application/json")
                        .content('{"username":"x","email":"x@x.com","password":"password123"}'))
                .andReturn()

        then:
        result.response.status != 401
    }

    // --- egzekwowanie uprawnień ---

    @WithMockUser(authorities = ["INCIDENT_READ"])
    def "użytkownik z uprawnieniem INCIDENT_READ może listować incydenty"() {
        when:
        def result = mockMvc.perform(get("/api/incidents")).andReturn()

        then:
        result.response.status == 200
    }

    @WithMockUser(authorities = ["INCIDENT_READ", "INCIDENT_REPORT"])
    def "użytkownik bez uprawnienia INCIDENT_ASSIGN dostaje 403 na przypisaniu"() {
        when:
        def result = mockMvc.perform(
                post("/api/incidents/some-id/assign")
                        .param("agentId", "agent1"))
                .andReturn()

        then:
        result.response.status == 403
    }

    @WithMockUser(authorities = ["INCIDENT_READ", "INCIDENT_ASSIGN"])
    def "użytkownik z uprawnieniem INCIDENT_ASSIGN przechodzi kontrolę uprawnień (błąd biznesowy, nie auth)"() {
        when:
        def result = mockMvc.perform(
                post("/api/incidents/nonexistent-id/assign")
                        .param("agentId", "agent1"))
                .andReturn()

        then:
        // Security passed - incydent nie istnieje, więc spodziewamy się 404
        result.response.status == 404
    }

    @WithMockUser(authorities = ["INCIDENT_READ"])
    def "użytkownik bez uprawnienia INCIDENT_REPORT dostaje 403 przy zgłaszaniu incydentu"() {
        when:
        def result = mockMvc.perform(
                post("/api/incidents")
                        .contentType("application/json")
                        .content('{"title":"T","description":"D","channel":"API","projectId":"P-1"}'))
                .andReturn()

        then:
        result.response.status == 403
    }

    @WithMockUser(authorities = ["INCIDENT_READ"])
    def "użytkownik bez uprawnienia SLA_READ dostaje 403 na endpoint SLA"() {
        when:
        def result = mockMvc.perform(get("/api/sla/violations")).andReturn()

        then:
        result.response.status == 403
    }

    @WithMockUser(authorities = ["SLA_READ"])
    def "użytkownik z uprawnieniem SLA_READ może listować naruszenia SLA"() {
        when:
        def result = mockMvc.perform(get("/api/sla/violations")).andReturn()

        then:
        result.response.status == 200
    }

    @WithMockUser(authorities = ["SLA_READ"])
    def "użytkownik bez uprawnienia SLA_WRITE nie może uruchomić sprawdzania SLA"() {
        when:
        def result = mockMvc.perform(post("/api/sla/check")).andReturn()

        then:
        result.response.status == 403
    }

    @WithMockUser(authorities = ["SLA_READ"])
    def "użytkownik bez uprawnienia SLA_WRITE nie może tworzyć polityk SLA"() {
        when:
        def result = mockMvc.perform(
                post("/api/sla/policies")
                        .contentType("application/json")
                        .content('{"projectId":"P","priority":"HIGH","responseTimeMinutes":10,"resolutionTimeMinutes":60,"penaltyAmount":100}'))
                .andReturn()

        then:
        result.response.status == 403
    }

    @WithMockUser(authorities = ["SLA_READ"])
    def "użytkownik bez uprawnienia SLA_WRITE nie może aktualizować polityk SLA"() {
        when:
        def result = mockMvc.perform(
                put("/api/sla/policies/1")
                        .contentType("application/json")
                        .content('{"projectId":"P","priority":"HIGH","responseTimeMinutes":10,"resolutionTimeMinutes":60,"penaltyAmount":100}'))
                .andReturn()

        then:
        result.response.status == 403
    }

    @WithMockUser(authorities = ["POSTMORTEM_READ"])
    def "użytkownik bez POSTMORTEM_WRITE nie może tworzyć raportu post-mortem"() {
        when:
        def result = mockMvc.perform(post("/api/incidents/some-id/post-mortem")).andReturn()

        then:
        result.response.status == 403
    }

    @WithMockUser(authorities = ["POSTMORTEM_READ", "POSTMORTEM_WRITE"])
    def "użytkownik bez POSTMORTEM_APPROVE nie może zatwierdzać raportu post-mortem"() {
        when:
        def result = mockMvc.perform(post("/api/incidents/some-id/post-mortem/approve")).andReturn()

        then:
        result.response.status == 403
    }
}
