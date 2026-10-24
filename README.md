# ApplyFlow

ApplyFlow is a full-stack job-search management application. It helps a candidate record job applications, monitor the hiring pipeline, schedule interviews, plan follow-ups, and export their data.

It is deliberately built as a small enterprise-style application:

- **Frontend:** React + TypeScript + Vite
- **Backend:** Java 17 + Spring Boot 3.4
- **Persistence:** PostgreSQL + Spring Data JPA + Flyway
- **Security:** Spring Security + stateless JWT authentication + BCrypt password hashing
- **API documentation:** Springdoc OpenAPI / Swagger UI
- **Local demo database:** H2 through a separate `local` Spring profile

## Product tour

After signing in, the user can:

1. View an overview dashboard with application, interview, and follow-up metrics.
2. Add an application with company, role, source, work mode, priority, status, and date.
3. Search the application list by company, job title, or status.
4. Edit or delete an application.
5. View the status history of an application.
6. Export owned applications to CSV.
7. Create and complete follow-up reminders.
8. Schedule and cancel upcoming interviews.
9. See unread reminder notifications.
10. Review pipeline analytics, including interview and offer rates.

The application is user-scoped: one user cannot read or modify another user's applications, follow-ups, interviews, or notifications.

## Live deployment: free-tier architecture

Free hosting providers change their limits and sleep policies, so confirm the current limits before deployment. A practical free setup is:

```text
Browser
   |
   v
Vercel or Netlify (React/Vite static frontend)
   |
   v
Render free web service (Spring Boot API)
   |
   v
Neon free PostgreSQL database
```

This separation is useful in an interview because each part can scale independently:

- A static frontend is cheap and fast to serve.
- The backend owns business rules and security.
- PostgreSQL provides durable relational storage.

### Important free-tier limitations

- A free backend service may sleep after inactivity, so the first request can be slow.
- Free databases usually have storage, compute, connection, and inactivity limits.
- Do not use the H2 `local` profile in production: it resets when the backend restarts.
- Never commit `.env` files, database passwords, or JWT secrets.
- Free hosting is suitable for a portfolio demo, not a production system with an uptime SLA.

### Recommended deployment order

#### 1. Create a PostgreSQL database

Create a free PostgreSQL database on Neon (or another PostgreSQL provider). Copy its JDBC connection details. The backend expects:

```text
DB_URL=jdbc:postgresql://HOST:5432/DATABASE?sslmode=require
DB_USERNAME=...
DB_PASSWORD=...
```

The exact JDBC URL depends on the provider. Use the provider's SSL connection details and do not paste credentials into Git.

#### 2. Deploy the backend

Create a new web service from the Git repository in Render:

```text
Root directory: backend
Build command: ./mvnw clean package -DskipTests
Start command: java -jar target/applyflow-api-0.0.1-SNAPSHOT.jar
```

On Windows, the repository uses `mvnw.cmd`; Linux build services use `mvnw`.

Set these backend environment variables in the hosting dashboard:

```text
DB_URL=jdbc:postgresql://...
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=<random secret of at least 32 bytes>
JWT_EXPIRATION_MINUTES=120
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.vercel.app
```

The application reads the hosting platform's `PORT` automatically through:

```yaml
server:
  port: ${PORT:8080}
```

After deployment, verify:

```text
https://your-backend-domain.onrender.com/actuator/health
https://your-backend-domain.onrender.com/swagger-ui.html
```

The health endpoint should return an `UP` response. Flyway runs the PostgreSQL migration when the application starts.

#### 3. Deploy the frontend

Create a Vercel or Netlify project from the same repository:

```text
Root directory: frontend
Build command: npm run build
Output directory: dist
```

Set this frontend environment variable:

```text
VITE_API_URL=https://your-backend-domain.onrender.com/api/v1
```

The `VITE_` prefix is required by Vite. Redeploy the frontend after changing it.

#### 4. Complete the CORS step

Copy the final frontend URL into the backend's `CORS_ALLOWED_ORIGINS` variable. For example:

```text
CORS_ALLOWED_ORIGINS=https://applyflow-demo.vercel.app
```

If you use more than one frontend origin, separate them with commas:

```text
CORS_ALLOWED_ORIGINS=https://applyflow-demo.vercel.app,https://www.example.com
```

Restart or redeploy the backend after changing CORS. Then register a new account and test the complete flow from the deployed frontend.

## Local setup

### Requirements

- Java 17+
- Node.js 18+
- npm
- Docker Desktop (recommended for PostgreSQL)
- Git

### Option A: PostgreSQL with Docker

From the repository root:

```powershell
docker compose up -d postgres
```

Start the backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Start the frontend in a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

### Option B: database-free local demo

The local profile uses an in-memory H2 database. Data is reset whenever the backend stops:

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

This is useful for an interview demo when Docker or PostgreSQL is unavailable. It is not a production deployment mode.

## Environment variables

### Backend

| Variable | Required | Purpose |
|---|---:|---|
| `DB_URL` | Yes for PostgreSQL | JDBC PostgreSQL connection string |
| `DB_USERNAME` | Yes for PostgreSQL | Database user |
| `DB_PASSWORD` | Yes for PostgreSQL | Database password |
| `JWT_SECRET` | Yes | HMAC signing secret; use at least 32 random bytes |
| `JWT_EXPIRATION_MINUTES` | No | Token lifetime; default is 120 |
| `CORS_ALLOWED_ORIGINS` | No | Comma-separated frontend origins |
| `PORT` | No | Hosting platform port; default is 8080 |

### Frontend

| Variable | Required | Purpose |
|---|---:|---|
| `VITE_API_URL` | No | API base URL; defaults to `http://localhost:8080/api/v1` |

The example files are [backend/.env.example](backend/.env.example) and [frontend/.env.example](frontend/.env.example). Environment files containing real secrets must remain untracked.

## Project structure

```text
ApplyFlow/
├── README.md
├── docker-compose.yml
├── .gitignore
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .env.example
│   └── src/
│       ├── main/
│       │   ├── java/com/applyflow/api/
│       │   │   ├── ApplyFlowApplication.java  # Spring Boot entry point and scheduling
│       │   │   ├── ApiModels.java             # Request/response DTO records
│       │   │   ├── Controllers.java           # REST controllers and endpoint mapping
│       │   │   ├── Domain.java                # JPA entities and enums
│       │   │   ├── Exceptions.java            # API exceptions and error handling
│       │   │   ├── Persistence.java            # Spring Data repositories
│       │   │   ├── Security.java              # JWT, BCrypt, CORS, authorization
│       │   │   └── Services.java              # Business logic and reminder scheduler
│       │   └── resources/
│       │       ├── application.yml            # Production/default configuration
│       │       ├── application-local.yml      # H2 local demo profile
│       │       └── db/migration/
│       │           └── V1__initial_schema.sql # Flyway database schema
│       └── test/java/com/applyflow/api/
│           ├── ApplicationServiceTest.java
│           └── DashboardServiceTest.java
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── index.html
│   ├── .env.example
│   └── src/
│       ├── main.tsx   # React UI, API calls, forms, navigation, state
│       ├── styles.css # Responsive visual system and component styles
│       └── vite-env.d.ts
└── docs/
    ├── CODE_WALKTHROUGH.md
    ├── DEMO_SCRIPT.md
    └── INTERVIEW_GUIDE.md
```

## Backend architecture

```text
HTTP request
  -> Controller
  -> Service
  -> Repository
  -> PostgreSQL
  -> DTO response
```

### Controllers

- `AuthController`: registration and login.
- `ApplicationController`: CRUD, pagination, history, and CSV export.
- `DashboardController`: user-scoped metrics.
- `FollowUpController`: create, list, and complete follow-ups.
- `InterviewController`: create, list, and cancel interviews.
- `NotificationController`: unread reminders and mark-as-read behavior.

### Services

- `AuthService` validates the account workflow and creates JWTs.
- `ApplicationService` owns application CRUD, status history, ownership checks, and CSV generation.
- `DashboardService` calculates counts, pipeline status, overdue follow-ups, interview rate, and offer rate.
- `ReminderScheduler` creates at most one notification per due follow-up.

### Persistence

Spring Data repository methods include the authenticated user ID in their queries. This is an important security boundary:

```java
findByIdAndUserId(applicationId, CurrentUser.id())
```

The database schema includes users, job applications, status history, follow-ups, interviews, and notifications. Foreign keys and uniqueness constraints preserve relationships and prevent duplicate reminders.

### Security flow

1. The user registers or logs in.
2. The server hashes the password with BCrypt.
3. The server returns a signed JWT.
4. The frontend sends `Authorization: Bearer <token>`.
5. `JwtFilter` validates the signature and extracts the user UUID.
6. `CurrentUser.id()` supplies the authenticated owner ID to services and repositories.
7. Invalid or expired tokens receive HTTP 401.

Passwords are never stored as plain text. JWTs are stateless, so the server does not need an in-memory HTTP session.

## API reference

Base URL: `http://localhost:8080/api/v1`

Protected endpoints require:

```http
Authorization: Bearer <jwt>
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Create an account |
| POST | `/auth/login` | Authenticate and return a JWT |
| GET | `/applications` | List owned applications with Spring pagination |
| POST | `/applications` | Create an application |
| PUT | `/applications/{id}` | Update an owned application |
| DELETE | `/applications/{id}` | Delete an owned application |
| GET | `/applications/{id}/history` | Read status timeline |
| GET | `/applications/export` | Export owned applications as CSV |
| GET | `/dashboard` | Return user-scoped dashboard metrics |
| GET | `/follow-ups` | List follow-ups |
| POST | `/follow-ups` | Create a follow-up |
| PATCH | `/follow-ups/{id}/complete` | Complete a follow-up |
| GET | `/interviews` | List upcoming interviews |
| POST | `/interviews` | Schedule an interview |
| PATCH | `/interviews/{id}/cancel` | Cancel an interview |
| GET | `/notifications` | List unread notifications |
| PATCH | `/notifications/{id}/read` | Mark a notification as read |

Swagger UI is available at `/swagger-ui.html` when the backend is running.

## Testing and verification

Run backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Build the frontend:

```powershell
cd frontend
npm run build
```

The backend tests cover application service behavior and dashboard calculations. The frontend build runs TypeScript checking before Vite produces the production bundle.

Before showing the hosted application to a recruiter, manually verify:

1. Register a new user.
2. Log out and log back in.
3. Add an application.
4. Edit its status and inspect history.
5. Search for the application.
6. Create a follow-up and complete it.
7. Schedule and cancel an interview.
8. Export CSV.
9. Open the app in a private browser window.
10. Confirm the health endpoint and Swagger page are reachable.

## Interview preparation: questions and strong answers

### 1. Explain your project.

**Answer:** ApplyFlow is a job-search tracker built with React and TypeScript on the frontend and Java Spring Boot on the backend. A user can manage applications, follow-ups, interviews, notifications, and pipeline analytics. The backend exposes REST APIs, validates requests, authenticates users with JWT, stores data in PostgreSQL through JPA, and runs Flyway migrations. I built it to demonstrate an end-to-end enterprise workflow rather than only a CRUD screen.

**Real example:** When I change an application from `APPLIED` to `INTERVIEW`, the service updates the application and writes a status-history record in the same transaction. The dashboard then recalculates the interview count.

### 2. Why did you choose Spring Boot?

**Answer:** Spring Boot provides dependency injection, embedded server support, REST controllers, validation, security integration, JPA, actuator health checks, and production configuration with less boilerplate. It is a strong fit for enterprise Java applications because responsibilities can be separated into controllers, services, repositories, and configuration.

### 3. Explain the request flow.

**Answer:** The React client sends a request to a controller. Spring Security authenticates the JWT before the controller runs. The controller validates the request DTO and delegates to a service. The service applies business rules and calls a repository. JPA translates the repository operation to SQL, PostgreSQL stores the data, and a response DTO is returned to the frontend.

### 4. Why use DTOs instead of returning entities?

**Answer:** DTOs keep the persistence model separate from the public API. This prevents accidental exposure of fields such as password hashes, avoids lazy-loading problems, and lets the API evolve without changing the database entity. In this project, records in `ApiModels.java` represent request and response contracts.

### 5. How does JWT authentication work?

**Answer:** Login verifies the BCrypt password and creates a signed JWT containing the user ID and email. The frontend stores the token and sends it as a Bearer token. `JwtFilter` validates the signature and places the user UUID in Spring Security's context. Repository queries then use that UUID to enforce ownership.

### 6. Why BCrypt?

**Answer:** BCrypt is a slow, salted password-hashing algorithm designed to make brute-force attacks more expensive. We store only the BCrypt hash, never the original password. During login, `PasswordEncoder.matches` compares the submitted password with the stored hash.

### 7. How do you prevent one user from seeing another user's data?

**Answer:** Authentication alone is not enough. Every ownership-sensitive repository query includes the current user ID, for example `findByIdAndUserId`. If the ID belongs to another user, the query returns no result and the API returns a not-found response. This check is performed in the service layer for applications, follow-ups, interviews, and notifications.

### 8. Why PostgreSQL instead of H2?

**Answer:** PostgreSQL is the durable production database and supports relational constraints, transactions, and concurrent access. H2 is included only as a convenient local profile so the application can be demonstrated without Docker. H2 data is intentionally temporary.

### 9. What is Flyway doing?

**Answer:** Flyway versions schema changes as migration files such as `V1__initial_schema.sql`. When the backend starts, Flyway applies pending migrations in order and records them in its schema-history table. This makes database changes repeatable across local, staging, and hosted environments.

### 10. How does the dashboard calculate interview rate?

**Answer:** `DashboardService` counts applications for the authenticated user, counts applications whose status is `INTERVIEW`, and divides the interview count by total applications. If there are no applications, it returns zero instead of dividing by zero. The same approach is used for the offer rate.

### 11. Why is status history written in a transaction?

**Answer:** The application update and history insert represent one business operation. `@Transactional` ensures both succeed together or both roll back. Without a transaction, the application could say `INTERVIEW` while its audit history still showed the previous state.

### 12. How do reminders avoid duplicates?

**Answer:** The scheduled reminder task finds incomplete follow-ups due today or earlier. Before inserting a notification, it checks whether a notification already exists for that follow-up. The database uniqueness constraint is the final protection against duplicates.

### 13. What validation exists?

**Answer:** Jakarta Bean Validation checks required fields, email format, password length, and maximum text lengths. Invalid payloads are converted to a structured API error with a validation code and field-level details. The frontend also uses required fields and appropriate input types for a better user experience.

### 14. How would you improve this for production?

**Answer:** I would add refresh tokens or an HTTP-only cookie strategy, rate limiting on authentication endpoints, structured logging, centralized metrics and tracing, database backups, CI/CD, integration tests with Testcontainers, pagination for every large collection, and role-based access if teams were added. I would also add HTTPS-only deployment, secret rotation, and stronger audit logging.

### 15. What happens when the backend is sleeping on a free host?

**Answer:** The first request can be slow while the service wakes up. The frontend displays a loading state and reports API failures instead of pretending the operation succeeded. For a production SLA, I would use a paid always-on instance or an autoscaling platform.

### 16. How would you test this application?

**Answer:** Unit tests cover service calculations and business rules. Repository tests verify ownership and persistence behavior. Controller integration tests verify HTTP status codes, validation, and security. End-to-end tests would register a user, create an application, update its status, and verify dashboard and history behavior.

### 17. What OOP concepts are present?

**Answer:** Encapsulation is used in domain entities, abstraction is provided by repository interfaces, polymorphism comes from Spring-managed interfaces and framework contracts, and dependency inversion is used when controllers and services depend on repository abstractions rather than concrete database code. Each service also follows single responsibility for a business area.

### 18. What was a practical bug you solved?

**Answer:** The frontend needed to send the JWT in the exact `Authorization: Bearer <token>` format expected by the Spring Security filter. A malformed header prevents every protected request from authenticating. Fixing that integration point restored dashboard loading and all authenticated operations. It is a good example of debugging across the frontend-backend boundary.

## Two-minute demo script

1. Register with a demo email.
2. Say: “This is a user-scoped job-search workspace.”
3. Add a Software Engineer application for a sample company.
4. Show the dashboard metric changing from zero to one.
5. Change the status to `INTERVIEW` and open history.
6. Schedule an interview and show it in the Interviews section.
7. Add a follow-up and complete it.
8. Open Analytics to explain the pipeline and conversion rates.
9. Export CSV.
10. Finish with: “The backend is a stateless Spring Boot REST API with JWT security, PostgreSQL persistence, Flyway migrations, validation, and scheduled reminders.”

## Troubleshooting

### Frontend says “Failed to fetch”

Check that the backend is running and that `VITE_API_URL` points to the correct `/api/v1` URL. On a hosted frontend, also check `CORS_ALLOWED_ORIGINS` on the backend.

### Login returns 401

Check the email and password, then check that `JWT_SECRET` is present and at least 32 bytes long. A backend restart invalidates no existing token unless the secret changes, but expired tokens must be replaced by logging in again.

### Backend fails during startup

Check `DB_URL`, database credentials, database availability, and migration logs. Do not switch silently to H2; the default production profile should fail clearly if PostgreSQL is unavailable.

### CORS errors after deployment

Set `CORS_ALLOWED_ORIGINS` to the exact deployed frontend origin, including `https://` and excluding a trailing path. Redeploy or restart the backend.

### No data after restart

Confirm which profile is active. The `local` profile uses in-memory H2 and intentionally resets. Use PostgreSQL for persistent data.

## Additional project material

- [docs/CODE_WALKTHROUGH.md](docs/CODE_WALKTHROUGH.md)
- [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md)
- [docs/INTERVIEW_GUIDE.md](docs/INTERVIEW_GUIDE.md)
