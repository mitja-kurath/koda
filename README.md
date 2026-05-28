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

## Deploying to temps.sh

[temps.sh](https://temps.sh) deploys this project from the root `docker-compose.yml` on every git push.

**1. Install the temps CLI and set up your server** (one-time):
```bash
curl -fsSL https://temps.sh/deploy.sh | bash
```

**2. Create the project and link your repo:**
```bash
temps projects create koda
temps git-providers add github --name "GitHub" --token "ghp_xxxx"
```

**3. Create an environment and inject secrets:**
```bash
temps environments create production

temps env set DB_PASSWORD="$(openssl rand -base64 32)" --environment production
temps env set JWT_SECRET="$(openssl rand -base64 48)" --environment production
temps env set CORS_ALLOWED_ORIGINS="https://your-domain.com" --environment production

# Optional overrides (defaults work for most setups)
# temps env set DB_NAME=koda --environment production
# temps env set DB_USER=koda --environment production
```

**4. Push to deploy** — temps detects `docker-compose.yml` and builds all three services automatically.

---

## Environment variables

Copy `.env.example` to `.env` before running with Docker Compose locally.

| Variable | Default | Required in prod | Description |
|---|---|---|---|
| `DB_NAME` | `koda` | | Database name |
| `DB_USER` | `koda` | | Database user |
| `DB_PASSWORD` | `koda` | Yes | Database password |
| `DB_PORT` | `5432` | | PostgreSQL port |
| `JWT_SECRET` | *(dev fallback)* | Yes | HMAC-SHA256 key, min 32 chars |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Yes | Public URL of the app |
| `SERVER_PORT` | `8080` | | API server port |

Override any of these locally by creating an `api/application-local.yaml` (git-ignored).
