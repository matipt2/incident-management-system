# Minimal Local PostgreSQL with Docker Compose

This project is a very small PostgreSQL setup for local or on-prem style development on macOS. It uses Docker Compose, the official PostgreSQL image, a Docker named volume for persistent data, and one SQL init file to create roles, schema, tables, and seed data.

## Files

- `docker-compose.yml`: starts PostgreSQL
- `.env.example`: example environment variables for the PostgreSQL superuser and database
- `init/01-init.sql`: creates roles, schema, tables, grants, indexes, and seed rows

## Why a named volume is used

A Docker named volume is used so the PostgreSQL data directory lives outside the container filesystem. That means the database files survive normal container removal and are kept by Docker until you explicitly remove the volume.

Conceptually, the database files live in Docker-managed storage, mounted into the container at:

`/var/lib/postgresql/data`

This makes the setup practical for local development because you can stop or recreate the container without losing data by accident.

## Important init behavior

The PostgreSQL image runs scripts from `/docker-entrypoint-initdb.d` only when the data directory is empty. In this project, that means:

- `init/01-init.sql` runs on the first initialization
- it does not run again on normal restarts
- if you want the init SQL to run again, you must remove the volume and start fresh

## Quick start

1. Create your local env file:

```bash
cp .env.example .env
```

2. Start PostgreSQL:

```bash
docker compose up -d
```

3. Follow logs if you want to watch initialization:

```bash
docker compose logs -f
```

## Stop the database

Stop and remove the container, but keep the named volume and its data:

```bash
docker compose down
```

## Reset everything from scratch

Stop the container and remove the named volume too:

```bash
docker compose down -v
```

Then start again:

```bash
docker compose up -d
```

Because the volume is gone, PostgreSQL sees an empty data directory and runs `init/01-init.sql` again.

## How to connect with psql from macOS

After copying `.env.example` to `.env`, connect from your Mac with:

```bash
psql -h localhost -p 5432 -U postgres -d incident_db
```

If you change `POSTGRES_DB` in `.env`, use that database name instead of `incident_db`.

When prompted, enter the `POSTGRES_PASSWORD` value from your `.env`.

You can also connect as one of the application roles created by the init SQL:

- `app_rw` password: `app_rw_dev_password`
- `app_ro` password: `app_ro_dev_password`

Example:

```bash
psql -h localhost -p 5432 -U app_rw -d incident_db
```

## How to connect using docker exec

Find the running container name from `docker ps`, or use the fixed name from this project:

```bash
docker exec -it <container> psql -U postgres -d incident_db
```

For this project, the container name is:

```bash
docker exec -it incident-postgres psql -U postgres -d incident_db
```

## Verify the seed data exists

Run a few quick checks:

```bash
docker exec -it incident-postgres psql -U postgres -d incident_db -c "SELECT id, email, user_type FROM app.users ORDER BY id;"
```

```bash
docker exec -it incident-postgres psql -U postgres -d incident_db -c "SELECT incident_key, status, priority FROM app.incident_log ORDER BY id;"
```

```bash
docker exec -it incident-postgres psql -U postgres -d incident_db -c "SELECT incident_id, action_type, created_at FROM app.incidents_history ORDER BY id;"
```

You should see:

- 3 rows in `app.users`
- 2 rows in `app.incident_log`
- a few audit/timeline rows in `app.incidents_history`

## Restart vs remove vs remove with volume

`restart container`

- example: `docker compose restart`
- the same container keeps using the same named volume
- data stays
- init SQL does not run again

`remove container`

- example: `docker compose down`
- the container is removed
- the named volume stays
- data stays
- init SQL does not run again on the next `up`

`remove container and volume`

- example: `docker compose down -v`
- the container is removed
- the named volume is removed
- data is deleted
- init SQL runs again next time because PostgreSQL starts with an empty data directory

## Plain-English summary

- the container runs PostgreSQL
- the named volume holds the real database files
- `init/01-init.sql` creates the roles, schema, tables, grants, indexes, and seed data on first initialization
- later, your application can connect with `app_rw` for read/write access or `app_ro` for read-only access

## Backend compatibility status (repo integrity check)

Current status: the database setup in this folder is valid on its own, but it is **not wired as the backend default persistence** in the current repository.

Why:

1. Backend datasource points to in-memory H2 by default (`backend/src/main/resources/application.properties`):
   - `spring.datasource.url=jdbc:h2:mem:itldb;DB_CLOSE_DELAY=-1`
   - `spring.datasource.driver-class-name=org.h2.Driver`

2. Backend runtime dependency is currently H2 (not PostgreSQL driver) in `backend/build.gradle`:
   - `runtimeOnly 'com.h2database:h2'`

3. Schema in `init/01-init.sql` does not match backend JPA table model:
   - PostgreSQL init creates: `app.users`, `app.incident_log`, `app.incidents_history`
   - Backend entities map to tables such as: `incidents`, `incident_events`, `post_mortem_reports`, `sla_policies`, `sla_violations`

Because of the above, this PostgreSQL package should currently be treated as a separate local DB setup/demo schema, not as a drop-in backend database for the present Spring Boot configuration.

## What to change to make backend use this PostgreSQL setup

- add PostgreSQL JDBC dependency in backend (`org.postgresql:postgresql`)
- switch datasource configuration from H2 to PostgreSQL connection properties
- align SQL schema with JPA entities (or introduce migrations and update mappings)

After these changes, backend and database module can work as one integrated persistence stack.
