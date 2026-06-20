package pl.edu.uj.projzes.itl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    def "rejestracja tworzy konto REPORTER bez tokenu JWT"() {
        given:
        def body = [username: "newuser", email: "new@example.com", password: "password123"]

        when:
        def result = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn()

        then:
        result.response.status == 201
        def json = objectMapper.readValue(result.response.contentAsString, Map)
        json.token == null
        json.username == "newuser"
        json.role == "REPORTER"
        json.permissions != null
    }

    def "publiczna rejestracja ignoruje próbę nadania roli AGENT"() {
        given:
        def body = [username: "agentuser", email: "agent@example.com", password: "password123", role: "AGENT"]

        when:
        def result = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn()

        then:
        result.response.status == 201
        def json = objectMapper.readValue(result.response.contentAsString, Map)
        json.role == "REPORTER"
        !json.permissions.contains("INCIDENT_ASSIGN")
    }

    def "rejestracja z istniejącą nazwą użytkownika zwraca 409"() {
        given:
        def body = [username: "duplicate", email: "first@example.com", password: "password123"]
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))

        def duplicate = [username: "duplicate", email: "other@example.com", password: "password123"]

        when:
        def result = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andReturn()

        then:
        result.response.status == 409
    }

    def "logowanie z poprawnymi danymi zwraca token JWT"() {
        given:
        def creds = [username: "loginuser", email: "login@example.com", password: "securepass1"]
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creds)))

        when:
        def result = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString([username: "loginuser", password: "securepass1"])))
                .andReturn()

        then:
        result.response.status == 200
        def json = objectMapper.readValue(result.response.contentAsString, Map)
        json.token != null
        json.username == "loginuser"
    }

    def "logowanie z błędnym hasłem zwraca 401"() {
        given:
        def creds = [username: "pwuser", email: "pw@example.com", password: "correctpass1"]
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creds)))

        when:
        def result = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString([username: "pwuser", password: "wrongpass"])))
                .andReturn()

        then:
        result.response.status == 401
    }

    def "logowanie nieistniejącego użytkownika zwraca 401"() {
        when:
        def result = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString([username: "nobody", password: "pass"])))
                .andReturn()

        then:
        result.response.status == 401
    }
}
