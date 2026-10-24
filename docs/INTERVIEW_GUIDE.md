# Interview guide

## Two-minute explanation

ApplyFlow solves the practical problem of losing track of job applications across multiple portals. A React client calls versioned Spring REST endpoints. Spring Security authenticates a BCrypt password at registration and signs a JWT at login. Controllers validate DTOs, services apply rules and transactions, repositories query PostgreSQL, and Flyway owns the schema.

The most important integrity rule is ownership: every application, follow-up, interview, and notification query includes the authenticated user's UUID. A status change writes the application and its history together. A scheduled job creates one in-app reminder per due follow-up, protected by a unique constraint.

## Questions to practise

1. **Why DTOs?** They prevent persistence details and password hashes from leaking into the API.
2. **Why constructor injection?** Dependencies are explicit and classes are straightforward to unit test.
3. **Why BCrypt?** It is deliberately slow and salted, making password guessing more expensive.
4. **What does JWT contain?** A signed subject UUID, email claim, issue time, and expiry; never a password.
5. **How is authorization enforced?** Repository methods query by both record ID and current user ID.
6. **Why Flyway?** Schema changes are versioned and reproducible across environments.
7. **What is a transaction?** A group of database writes that commits or rolls back as one unit.
8. **Why record status history?** It provides an auditable timeline instead of overwriting context.
9. **How is duplicate reminder creation prevented?** `existsByFollowUpId` plus a database unique constraint.
10. **How are empty conversion rates handled?** The denominator is total unique applications; zero applications returns 0, not NaN.
11. **Where is pagination applied?** Spring Data `Pageable` is passed to the repository, so the server does not load all rows.
12. **What is an N+1 query?** Repeated lazy queries for a collection; this design returns compact projections/views for list endpoints.
13. **What does `ddl-auto=validate` do?** It checks mappings against the migration schema without altering it.
14. **Why use enums?** Status and source values are constrained at the Java and API boundaries.
15. **How would you add search?** Add parameterized repository predicates for the scoped user query and indexes for common filters.
16. **How would you test authorization?** Authenticate two UUIDs and assert that the second receives 404 for the first's ID.
17. **What is BCrypt's salt?** A random value stored inside the encoded hash so equal passwords do not produce equal hashes.
18. **Why stateless sessions?** The server can validate each JWT without storing an HTTP session.
19. **What does `@Valid` do?** It invokes Jakarta Bean Validation before the controller method executes.
20. **Why return 404 for another user's ID?** It avoids confirming whether a record exists to an unauthorized caller.
21. **How can indexes help?** The user/status and user/applied-date indexes accelerate dashboard and filter queries.
22. **What is the trade-off of JWT?** Revocation is harder than sessions; short expiry and future refresh-token support are mitigations.
23. **How does scheduling work?** Spring invokes the annotated method using the configured cron expression.
24. **How would you add email?** Keep the notification creation service and add a delivery adapter; local operation remains in-app.
25. **Why use `Instant` for event times?** It represents an unambiguous UTC point; the client localizes it.
26. **What is a lazy relationship?** Related rows are loaded only when accessed, reducing unnecessary list queries.
27. **How does React use real data?** Its `api` helper sends the JWT to the Spring endpoints and renders the JSON responses.
28. **What should not be committed?** Database passwords, JWT secrets, build output, and dependency folders.
29. **How would you improve analytics scale?** Add grouped SQL projections rather than collecting entities in memory.
30. **What is a future improvement?** Refresh tokens, saved filter views, CSV export, and Testcontainers in CI.

Be honest in an interview: explain the code you have studied, identify limitations, and describe the tests you actually ran.
