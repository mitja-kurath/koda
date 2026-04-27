# Koda

API documentation platform built as a school project for security module Modul 183.

Upload an OpenAPI specification, browse and render its endpoints, and manage access with JWT-based authentication.

## Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 21, Tailwind CSS 4 |
| Backend | Spring Boot 4, Java 25 |
| Database | PostgreSQL 17 (migrations via Flyway) |
| Monorepo | pnpm Workspaces |

## Prerequisites

- [Node.js 22+](https://nodejs.org) and [pnpm 10](https://pnpm.io)
- [Java 25](https://adoptium.net)
- [Docker](https://www.docker.com/products/docker-desktop) (for the database)

## Development

Start all three services with a single command:

```bash
pnpm dev
```

This runs the Angular dev server and Spring Boot in parallel. Spring Boot automatically starts the PostgreSQL container via its Docker Compose integration — no separate DB step required.

| Service | URL |
|---|---|
| Web UI | http://localhost:4200 |
| API | http://localhost:8080 |
| Database | localhost:5432 |

## Full Docker deployment

Build and run the entire stack in containers:

```bash
docker-compose up --build
```

| Service | URL |
|---|---|
| Web UI | http://localhost:4200 |
| API | http://localhost:8080 |

Stop and remove containers:

```bash
docker-compose down
```

## Project structure

```
koda/
├── api/          # Spring Boot application
│   ├── src/main/java/docs/koda/api/
│   ├── src/main/resources/
│   │   ├── application.yaml
│   │   └── db/migration/   # Flyway SQL migrations
│   └── compose.yml         # PostgreSQL for local dev
├── web/          # Angular application
│   └── src/
└── docker-compose.yml      # Full-stack container setup
```

## Environment variables

The API reads the following variables at startup (defaults shown):

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `koda` | Database name |
| `DB_USER` | `koda` | Database user |
| `DB_PASSWORD` | `koda` | Database password |
| `SERVER_PORT` | `8080` | API server port |

Override any of these by creating an `api/application-local.yaml` (git-ignored).
