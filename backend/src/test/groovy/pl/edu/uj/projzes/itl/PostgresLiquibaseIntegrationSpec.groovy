package pl.edu.uj.projzes.itl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import pl.edu.uj.projzes.itl.application.IncidentService
import pl.edu.uj.projzes.itl.infrastructure.persistence.IncidentRepository
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@Transactional
class PostgresLiquibaseIntegrationSpec extends Specification {

    @Shared
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("incident_db")
            .withUsername("incident_user")
            .withPassword("incident_password")

    static {
        postgres.start()
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
        registry.add("spring.datasource.driver-class-name", { "org.postgresql.Driver" })
        registry.add("spring.jpa.hibernate.ddl-auto", { "validate" })
        registry.add("spring.liquibase.enabled", { "true" })
        registry.add("itl.sla.scheduler.enabled", { "false" })
    }

    @Autowired
    IncidentService incidentService

    @Autowired
    IncidentRepository incidentRepository

    def "Spring starts with PostgreSQL and Liquibase schema supports incident persistence"() {
        when:
        def incident = incidentService.reportIncident(
                "Postgres smoke",
                "Liquibase schema verification",
                "postgres-test",
                "API",
                "PROJ-PG"
        )

        then:
        incident.id != null
        incidentRepository.findById(incident.id).isPresent()
    }

    def cleanupSpec() {
        postgres.stop()
    }
}
