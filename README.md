# ITL — System zarządzania incydentami (mini-ITIL)

## Wymagania

- Java 21+
- Gradle

## Struktura projektu

```
incident-management-system/
├── backend/                        # Java 21, Spring Boot 4
│   └── src/main/java/.../itl/
│       ├── api/                    # Kontrolery REST + DTOs
│       │   ├── IncidentController
│       │   ├── SlaController
│       │   ├── PostMortemController
│       │   └── dto/
│       ├── application/            # Serwisy (logika aplikacji)
│       │   ├── IncidentService
│       │   ├── ClassificationService
│       │   ├── SlaService
│       │   └── PostMortemService
│       ├── domain/                 # Model domenowy
│       │   ├── incident/
│       │   ├── sla/
│       │   └── postmortem/
│       └── infrastructure/
│           ├── config/
│           ├── llm/
│           └── persistence/
├── database/                       # PostgreSQL (Docker Compose + init SQL)
│   ├── docker-compose.yml
│   ├── .env.example
│   └── init/01-init.sql
├── frontend/                       
├── .github/workflows/              
└── PROJEKT.md                      # Opis wymagań
```

## Uruchomienie backendu

```bash
./gradlew :backend:bootRun
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Testy

```bash
./gradlew :backend:test
```