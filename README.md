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

## Pierwszy manager

Publiczna rejestracja zawsze tworzy konto z rolą `REPORTER`. Pierwsze konto
managera można utworzyć przy starcie aplikacji przez zmienne środowiskowe:

```bash
export ITL_BOOTSTRAP_MANAGER_USERNAME=manager
export ITL_BOOTSTRAP_MANAGER_EMAIL=manager@example.com
export ITL_BOOTSTRAP_MANAGER_PASSWORD='change-me'
```

Konto jest tworzone tylko wtedy, gdy w bazie nie istnieje jeszcze żaden
manager. W środowisku produkcyjnym należy również ustawić własny `JWT_SECRET`.

## Uruchomienie przez Docker Compose

```bash
docker compose up --build
```

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

Główny `docker-compose.yml` buduje obrazy FE/BE i uruchamia PostgreSQL. Backend domyślnie używa PostgreSQL, a schemat bazy jest zarządzany przez Liquibase po stronie backendu.

Zmienne `ITL_BOOTSTRAP_MANAGER_USERNAME`, `ITL_BOOTSTRAP_MANAGER_EMAIL` i
`ITL_BOOTSTRAP_MANAGER_PASSWORD` można przekazać również do `docker compose`.

## Role i uprawnienia

- `REPORTER` zgłasza incydenty i widzi własne zgłoszenia.
- `AGENT` widzi przypisane incydenty oraz może je klasyfikować, eskalować i rozwiązywać.
- `VIEWER` ma dostęp tylko do odczytu wszystkich incydentów.
- `MANAGER` zarządza projektami, rolami użytkowników, przypisaniami i zamykaniem incydentów.

Manager nie może obniżyć własnej roli ani roli ostatniego managera.

## Projekty i workflow incydentów

Projekty mają niezmienny klucz, nazwę, opis i status aktywności. Nieaktywny
projekt pozostaje widoczny w danych historycznych, ale nie można dla niego
tworzyć nowych incydentów ani zmieniać polityk SLA.

Główne endpointy administracyjne:

```text
GET    /api/projects
GET    /api/projects/{key}
POST   /api/projects
PUT    /api/projects/{key}
PATCH  /api/projects/{key}/status
GET    /api/management/users
PATCH  /api/management/users/{id}/role
```

Dozwolone przejścia statusów:

```text
assignment: NEW -> IN_PROGRESS
reassignment: IN_PROGRESS or ESCALATED
escalation: NEW or IN_PROGRESS -> ESCALATED
resolution: IN_PROGRESS or ESCALATED -> RESOLVED
closure: RESOLVED -> CLOSED (manager only)
```

Zamknięcie incydentu krytycznego nadal wymaga zatwierdzonego post-mortem.
Niedozwolone przejścia zwracają `409 Conflict`.

Frontend udostępnia pełny workflow:

- klasyfikację priorytetu i kategorii przez przypisanego agenta lub managera,
- przypisanie, eskalację, rozwiązanie i zamknięcie incydentu,
- utworzenie, edycję i zatwierdzenie post-mortem,
- podgląd naruszeń SLA na szczegółach incydentu,
- zarządzanie politykami SLA i karami na stronie `/sla`.

## Uruchomienie całego stacka lokalnie

Na macOS/Linux można uruchomić bazę, backend i frontend jednym skryptem:

```bash
bash ./scripts/run-local.sh
```

Skrypt uruchamia główny `docker-compose.yml`, buduje obrazy FE/BE i startuje PostgreSQL.

Wariant deweloperski z managerem `manager / manager123`:

```bash
bash ./scripts/run-local-seeded.sh
```

Liquibase tworzy również projekty `PROJ-1` i `PROJ-SLA`. Skrypt dodaje incydent
`DEMO-SLA-1`, zgłoszony trzy godziny wcześniej, oraz uruchamia sprawdzenie SLA.
Na stronie `/sla` są dzięki temu widoczne przykładowe naruszenia czasu reakcji
i rozwiązania z karą oczekującą na zastosowanie. Dane logowania można
nadpisać zmiennymi `ITL_BOOTSTRAP_MANAGER_USERNAME`,
`ITL_BOOTSTRAP_MANAGER_EMAIL` i `ITL_BOOTSTRAP_MANAGER_PASSWORD`.

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

Testy backendu używają PostgreSQL przez Testcontainers i uruchamiają changelog Liquibase.
Docker musi być dostępny.

Test integracyjny PostgreSQL:

```bash
./gradlew :backend:postgresIntegrationTest
```

Ten test uruchamia PostgreSQL przez Testcontainers, aplikuje migracje i wymaga dostępnego Dockera.

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

