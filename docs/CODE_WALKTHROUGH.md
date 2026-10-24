# Code walkthrough

1. `ApplyFlowApplication` starts Spring Boot and enables scheduled reminders.
2. `Security.java` hashes passwords through BCrypt and verifies the JWT before a request reaches a controller. The token subject is the user UUID.
3. `Controllers.java` translates HTTP requests to DTOs. It never queries JPA directly for business rules.
4. `Services.java` owns account creation, application status transitions, metrics, and reminder idempotency.
5. `Persistence.java` contains repositories. User-owned queries include `userId` so changing an ID cannot cross account boundaries.
6. `Domain.java` contains the relational model. `JobApplication` has many `StatusHistory` rows; follow-ups and interviews optionally reference an application.
7. `V1__initial_schema.sql` is the repeatable local database contract. `ddl-auto=validate` prevents Hibernate from silently changing production schema.

The browser stores only the short-lived access token. Application records remain in PostgreSQL and are fetched through the API. The dashboard counts are calculated from repository queries and explicitly return zero rates when there are no applications.
