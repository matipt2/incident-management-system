# ITL — System zarządzania incydentami (mini-ITIL)

## Wymagania

- Java 21+
- Gradle
- Node.js 22+
- Docker

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
├── database/                       # PostgreSQL lokalnie (Docker Compose)
│   ├── docker-compose.yml
│   └── .env.example
├── frontend/                       
├── .github/workflows/              
└── PROJEKT.md                      # Opis wymagań
```

## Uruchomienie backendu

Backend domyślnie używa PostgreSQL. Przed startem aplikacji trzeba uruchomić lokalną bazę:

```bash
cd database
cp .env.example .env
docker compose up -d
cd ..
```

```bash
./gradlew :backend:bootRun
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Uruchomienie przez Docker Compose

```bash
docker compose up --build
```

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

Główny `docker-compose.yml` buduje obrazy FE/BE i uruchamia PostgreSQL. Backend domyślnie używa PostgreSQL, a schemat bazy jest zarządzany przez Liquibase po stronie backendu.

## Uruchomienie całego stacka lokalnie

Na macOS/Linux można uruchomić bazę, backend i frontend jednym skryptem:

```bash
bash ./scripts/run-local.sh
```

Skrypt uruchamia główny `docker-compose.yml`, buduje obrazy FE/BE i startuje PostgreSQL.

Domyślna konfiguracja bazy:

```text
jdbc:postgresql://localhost:5432/incident_db
incident_user / incident_password
```

## Testy

Testy backendu:

```bash
./gradlew :backend:test
```

Te testy uruchamiają się z profilem `test`, który używa PostgreSQL przez Testcontainers. Docker musi być dostępny.

## Frontend 
### http://localhost:5173
```bash
cd frontend
npm install
npm run dev
```

## Testy frontendu

```bash
cd frontend
npm run test:unit
npm run test:e2e:ci
```

