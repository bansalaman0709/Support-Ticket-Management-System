# Support Ticket Management System

A simple support ticket app: create, list, search, filter, update tickets, add comments, and change status through a web UI backed by a REST API.

---

## What you need installed

| Tool | Version | Why |
|------|---------|-----|
| **Java** | **21** | Backend runtime |
| **Maven** | 3.9+ | Build and run the backend |
| **Node.js** | 20+ (includes npm) | Frontend |
| **PostgreSQL** | 14+ | Application database |

Check versions:

```bash
java -version
mvn -version
node -v
npm -v
psql --version
```

---

## 1. Create the database

1. Start PostgreSQL.
2. Create a database named `support_tickets` (once):

```bash
psql -U postgres -h localhost -c "CREATE DATABASE support_tickets;"
```

If the database already exists, you can skip this step.

Default connection used by the app:

| Setting | Default |
|---------|---------|
| Host / port | `localhost:5432` |
| Database | `support_tickets` |
| Username | `postgres` |
| Password | *(none — you must set it)* |

---

## 2. Start the backend

Open a terminal in the **`backend`** folder (important — do not run Maven from the repo root).

### Windows (PowerShell)

```powershell
cd backend

# Required: your PostgreSQL password for user "postgres"
$env:DB_PASSWORD = "YOUR_POSTGRES_PASSWORD"

# Optional overrides
# $env:DB_URL = "jdbc:postgresql://localhost:5432/support_tickets"
# $env:DB_USERNAME = "postgres"
# $env:SERVER_PORT = "8090"

mvn spring-boot:run
```

### macOS / Linux

```bash
cd backend
export DB_PASSWORD="YOUR_POSTGRES_PASSWORD"
# export SERVER_PORT=8090
mvn spring-boot:run
```

When it starts successfully you should see something like:

`Started SupportTicketApplication`

API base URL (default): **http://localhost:8090/api**

Quick check:

```bash
curl http://localhost:8090/api/tickets
```

You should get `[]` or a JSON list of tickets.

### If Maven cannot reach the internet

Build once when online, then run the JAR:

```bash
cd backend
mvn -DskipTests package
java -jar target/support-ticket-backend-0.0.1-SNAPSHOT.jar
```

(Still set `DB_PASSWORD` in the same terminal before `java -jar`.)

### If port 8090 is already in use

```powershell
$env:SERVER_PORT = "8091"
```

Then point the frontend proxy at the same port (see troubleshooting below).

---

## 3. Start the frontend

Open a **second** terminal in the **`frontend`** folder:

```bash
cd frontend
npm install
npm run dev
```

Open the UI in your browser:

**http://localhost:5173/**

The Vite dev server proxies `/api` calls to the backend (default **http://localhost:8090**).

---

## 4. Use the app

From the UI you can:

1. **Create** a ticket (title required; description, priority, assignee optional).
2. **List** tickets on the home page.
3. **Search** by keyword and **filter** by status.
4. Open a ticket to **view details**, **edit** fields, **add comments**, and **change status**.

Allowed status transitions (enforced by the backend):

- `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`
- `OPEN` → `CANCELLED`
- `IN_PROGRESS` → `CANCELLED`

Invalid transitions are rejected with an error message in the UI.

---

## Project layout

```
Support-Ticket-Management-System/
├── backend/          Spring Boot REST API (Java 21)
├── frontend/         React + TypeScript UI (Vite)
├── spec/             Product specifications
└── README.md         This file
```

---

## Configuration reference

Environment variables for the backend:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/support_tickets` | JDBC URL |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | *(empty)* | DB password — **required** for most PostgreSQL installs |
| `SERVER_PORT` | `8090` | Backend HTTP port |

Do **not** commit real passwords. Prefer setting them in your terminal session, or in a local file such as `backend/.env.local` that is already ignored by git.

---

## Troubleshooting

### `No plugin found for prefix 'spring-boot'`

You ran Maven outside the `backend` folder. Run:

```bash
cd backend
mvn spring-boot:run
```

### `Connect timed out` to `repo.maven.apache.org`

Network cannot reach Maven Central. Use a previously built JAR (`mvn package` then `java -jar ...`), or fix network/proxy access and retry.

### `SCRAM-based authentication, but no password was provided`

Set `DB_PASSWORD` to your PostgreSQL password before starting the backend.

### `Port … was already in use`

Another process is using that port. Either stop it, or set `SERVER_PORT` to a free port and update `frontend/vite.config.ts` so the `/api` proxy `target` matches (for example `http://localhost:8091`).

### UI loads but tickets fail / network errors

1. Confirm the backend is up: http://localhost:8090/api/tickets  
2. Confirm the frontend proxy target matches `SERVER_PORT`.  
3. Refresh the browser.

### Wrong Java version

The backend needs **Java 21**. If `java -version` shows 8 or 17, install JDK 21 and point `JAVA_HOME` at it.

---

## Running tests (optional)

**Backend:**

```bash
cd backend
mvn test
```

**Frontend:**

```bash
cd frontend
npm test
```

---

## Tech stack

- Backend: Java 21, Spring Boot, Spring Data JPA, PostgreSQL  
- Frontend: React, TypeScript, Vite  
- API: REST under `/api`
