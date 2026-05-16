# Local PostgreSQL

This folder provides a local PostgreSQL container for running the backend against a real database.

Application tables are **not** created by SQL files in this folder. The backend owns the schema through Liquibase:

```text
backend/src/main/resources/db/changelog/db.changelog-master.yaml
```

## Start PostgreSQL

```bash
cd database
cp .env.example .env
docker compose up -d
```

Defaults:

```text
database: incident_db
user: incident_user
password: incident_password
port: 5432
```

## Run Backend Against PostgreSQL

From the repository root:

```bash
./gradlew :backend:bootRun
```

On startup, Liquibase creates or updates the schema and Hibernate validates the entity mappings.

## Reset Local Database

```bash
cd database
docker compose down -v
docker compose up -d
```

The next backend startup will run Liquibase from scratch.

## Run PostgreSQL Integration Tests

From the repository root:

```bash
./gradlew :backend:postgresIntegrationTest
```

This task starts PostgreSQL through Testcontainers, applies Liquibase, and verifies the backend can persist data against the real database engine. Docker must be available.
