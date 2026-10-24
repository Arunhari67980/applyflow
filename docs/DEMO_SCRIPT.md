# Five-minute demo

1. Start PostgreSQL, backend, and frontend using the README commands.
2. Register a new account and explain that BCrypt stores only a one-way password hash.
3. Show the dashboard's empty state, then create an application with a source, priority, and applied date.
4. Change its status to `INTERVIEW`; show `/applications/{id}/history` in Swagger to demonstrate the timeline.
5. Add a follow-up and an interview. Complete the follow-up and refresh the dashboard.
6. Sign out, register a second account, and explain that the first account's UUID-scoped queries cannot return the first user's rows.

Useful API request:

```json
POST /api/v1/applications
{
  "companyName": "Northstar Labs",
  "jobTitle": "Java Software Engineer",
  "source": "LINKEDIN",
  "jobType": "FULL_TIME",
  "workMode": "HYBRID",
  "appliedDate": "2026-10-23",
  "priority": "HIGH",
  "status": "APPLIED"
}
```
