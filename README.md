# Employee Management System — Backend

Spring Boot REST API with JWT authentication, role-based access control, and audit trails.

## Tech stack
- Java 17, Spring Boot, Spring Security, Spring Data JPA
- MySQL
- JWT (jjwt)

## Setup
1. Create a MySQL database named `spring`.
2. Update `src/main/resources/application.properties` with your DB credentials.
3. Run the SQL scripts in `/schema` (if you export them) to create tables, or let Hibernate auto-create with `ddl-auto=update`.
4. Run `DemoApplication.java`.
5. API runs on `http://localhost:8080`.

## Key endpoints
- `POST /api/auth/login` — get a JWT
- `GET /api/employees`, `/api/departments`, `/api/users` (admin only)
