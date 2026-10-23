# Employee Management System — Backend Documentation

**Stack:** Spring Boot 3.5.6 · Java 17 · Spring Security 6 · Spring Data JPA (Hibernate) · MySQL 8 · JWT (jjwt 0.12.6) · Maven
**Base package:** `com.example.demo`
**Server port:** `8080` · **DB:** `jdbc:mysql://localhost:3306/spring`

---

## 1. Architecture at a Glance

```
com.example.demo
├── config/        Security, JWT, auditing, login-attempt/token-blacklist infra
├── controller/     REST endpoints (thin — delegate to service layer)
├── service/        Business logic
├── repository/      Spring Data JPA interfaces
├── model/          JPA entities + enums
├── security/       Custom Spring Security principal
└── exception/       Custom exceptions + centralized error handling
```

**Request flow:** `Client → CORS filter → JwtFilter → DispatcherServlet → Controller (@PreAuthorize check) → Service → Repository → MySQL`

**Scheduled jobs:** `@EnableScheduling` is active on `DemoApplication`. Two background cleanup tasks run automatically: hourly purge of expired blacklisted tokens, daily purge of stale login-attempt lockout rows (see §7).

---

## 2. Authentication & Session Model

| Piece | File | Role |
|---|---|---|
| Login/logout/refresh endpoints | `controller/AuthController.java` | Issues JWTs, tracks sessions, brute-force lockout, returns the user's authorities |
| JWT creation/parsing | `config/JwtUtil.java` | HMAC-signed, secret sourced from `app.jwt.secret` (env-overridable) |
| Per-request auth | `config/JwtFilter.java` | Reads `Authorization: Bearer …`, checks the blacklist, populates `SecurityContext` |
| Refresh tokens | `model/RefreshToken.java`, `repository/RefreshTokenRepository.java` | 7-day opaque UUID tokens, stored in DB, carry the originating `sessionId` |
| Logout / revocation | `config/TokenBlacklistService.java`, `model/BlacklistedToken.java` | DB-backed blacklist of logged-out access tokens |
| Brute-force lockout | `config/LoginAttemptService.java`, `model/LoginAttempt.java` | DB-backed failed-attempt counter, blocks after 5 failed attempts per username |
| Activity tracking | `model/UserSession.java`, `repository/UserSessionRepository.java` | One row per login, closed out (duration calculated) on logout |

### JWT configuration (`application.yml`)
```yaml
app:
  jwt:
    secret: ${JWT_SECRET:this-is-a-demo-secret-key-change-me-before-production-use}
    expiration-ms: 900000
```
Set `JWT_SECRET` (32+ chars) as an environment variable in any real deployment.

### Login flow (`POST /api/auth/login`)
1. `LoginAttemptService.isBlocked()` → `429` if the account is locked out from repeated failures.
2. Authenticate via `AuthenticationManager` (delegates to `CustomUserDetailsService` + BCrypt).
3. On failure: record the failed attempt, return `401`.
4. On success: clear the attempt record, create a `UserSession` row, mint a JWT carrying the session ID as a `sid` claim, create a refresh token linked to that same session, and return:
   ```json
   {
     "token": "...",
     "refreshToken": "...",
     "username": "...",
     "role": "ADMIN",
     "authorities": ["ROLE_ADMIN", "EDIT_ALL_PROFILE", "MANAGE_EMPLOYEES", "..."]
   }
   ```
   The `authorities` array is built fresh from `RoleAuthorityRepository.findByRoleCode(user.getRole().getRoleCode())` — the same source of truth the JWT's own `GrantedAuthority` list is built from via `CustomUserDetailsService`, so what the frontend receives always matches what the backend actually enforces.

### Logout flow (`POST /api/auth/logout`)
Requires a valid, authenticated request (no longer publicly reachable — see §3.4). Blacklists the current access token (persisted with its real expiry so cleanup is accurate), closes out the `UserSession`, deletes the user's refresh token.

### Refresh flow (`POST /api/auth/refresh`)
Exchanges a valid, unexpired refresh token for a new access token, preserving the original session's `sid` claim and returning the same `authorities` array shape as login (so a refreshed session doesn't silently lose its permission list on the frontend).

---

## 3. Authorization / RBAC Model

This is now a **fully data-driven RBAC system** — roles and their authorities are managed through the app itself (`/api/roles`, `/api/authorities`), not hardcoded. Two layers work together:

1. **URL-level** (`SecurityConfig.filterChain`) — coarse gating before the request reaches a controller.
2. **Method-level** (`@PreAuthorize` on individual controller methods) — fine-grained, using SpEL for ownership checks.

### 3.1 Roles & Authorities schema

Three DB tables:
- `role` (`Role.java`) — auto-increment `id`, `role_code` (e.g. `ADMIN`), `role_name`
- `authority` (`Authority.java`) — just a `name` string PK (e.g. `MANAGE_EMPLOYEES`)
- `role_authority` (`RoleAuthority.java`) — join table mapping `role_code` → `authority_name`, unique-constrained on the pair, FK-constrained against `authority.name`

At login, `CustomUserDetailsService.loadUserByUsername()` pulls every `role_authority` row for the user's role code and turns each `authority_name` into a `SimpleGrantedAuthority`.

### 3.2 Managing roles & authorities (`RoleController`, `AuthorityController`)

Both controllers are class-level `@PreAuthorize("hasRole('ADMIN')")` — only Admins can create roles, create authorities, or reassign what a role can do.

**`/api/authorities`** (`AuthorityController`):
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/` | List every authority that exists |
| POST | `/` | Create a new authority (name is uppercased, spaces → underscores, e.g. `manage invoices` → `MANAGE_INVOICES`) |

**`/api/roles`** (`RoleController`):
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/` | List all roles |
| POST | `/` | Create a role, optionally with an initial set of authority names attached — `@Transactional`, so a bad authority name rolls back the whole create |
| GET | `/{roleCode}` | Get one role |
| DELETE | `/{roleCode}` | Delete a role **and** its `role_authority` rows |
| GET | `/{roleCode}/authorities` | List the authorities currently attached to a role |
| POST | `/{roleCode}/authorities` | Attach one authority to a role (rejects if the authority doesn't exist, or is already attached) |
| DELETE | `/{roleCode}/authorities/{authorityName}` | Detach one authority from a role |

⚠️ **No protection on deleting the `ADMIN` role itself, or a role that's still assigned to active users.** Deleting a role in use sets those users' `role_id` to `NULL` (this has already happened once during development — see the git history / support conversation for the recovery steps). Worth adding a guard here: block deletion if `role_code = 'ADMIN'`, and/or block deletion if any `AppUser` still references it.

### 3.3 Current role capability matrix (default seed data)

| Capability | ADMIN | SUPERVISOR | USER |
|---|:---:|:---:|:---:|
| View dashboard / activity graphs (`/api/sessions/**`) | ✅ | ✅ | ❌ |
| Create employee (`MANAGE_EMPLOYEES`) | ✅ | ✅ | ❌ |
| Create/edit/delete department (`MANAGE_DEPARTMENTS`) | ✅ | ✅ | ❌ |
| Edit **any** employee record (`EDIT_ALL_PROFILE`) | ✅ | ❌ | ❌ |
| Edit **own** employee record | ✅ | ✅ | ✅ |
| Delete employee (`DELETE_EMPLOYEE`) | ✅ | ❌ | ❌ |
| Manage users / roles / authorities (`hasRole('ADMIN')`) | ✅ | ❌ | ❌ |

Because roles are now fully custom, this table only describes the *default* seed data — an admin can create any additional role (e.g. `TEAM_LEAD`) with any combination of existing authorities via the Roles UI, without touching code.

### 3.4 The "edit own record" mechanism

1. `security/CustomUserDetails.java` extends Spring's `User`, adding `employeeId`, `userId`, `roleCode`.
2. `CustomUserDetailsService` populates `employeeId` via `EmployeeRepository.findByAppUser_UserId(user.getUserId())`.
3. `EmployeeController.updateEmployee`:
   ```java
   @PreAuthorize("hasAuthority('EDIT_ALL_PROFILE') or #empId == authentication.principal.employeeId")
   ```
**Gotcha (unchanged):** `employeeId` is resolved once at login and baked into that session — changes to a user's linked employee record don't take effect until their next login.

### 3.5 `SecurityConfig.java` — URL rules (in order)

```
OPTIONS /**                       → permitAll
POST /api/auth/login               → permitAll
POST /api/auth/refresh             → permitAll
/photos/**                         → permitAll
/api/sessions/**                   → hasAnyRole(ADMIN, SUPERVISOR)
/api/users/**                      → hasRole(ADMIN)
/api/roles/**                      → hasRole(ADMIN)
/api/authorities/**                → hasRole(ADMIN)
DELETE /api/**                     → hasRole(ADMIN)
/api/**                            → authenticated   ← POST /api/auth/logout falls under this
everything else                     → permitAll
```
`/api/auth/logout` is intentionally **not** in the public list — it requires a valid authenticated request, closing an earlier gap where a missing/invalid token used to silently no-op instead of being rejected.

⚠️ **Still-open deprecation:** `DaoAuthenticationProvider()` + `.setUserDetailsService()` is deprecated in Spring Security 6.3+ (functional, but should move to constructor injection).

---

## 4. Data Model (entities)

All entities except `Authority`, `RefreshToken`, `UserSession`, `BlacklistedToken`, and `LoginAttempt` extend `Auditable`, auto-populating `createdBy`/`createdDate`/`lastModifiedBy`/`lastModifiedDate`.

| Entity | Table | Key fields | Relationships |
|---|---|---|---|
| `AppUser` | `users` | `userId`, `username` (unique), `password` (BCrypt, `@JsonIgnore`) | `ManyToOne → Role` |
| `Role` | `role` | `id`, `roleCode` (unique), `roleName` | — |
| `Authority` | `authority` | `name` (PK) | referenced by `role_authority.authority_name` FK |
| `RoleAuthority` | `role_authority` | `roleCode`, `authorityName` (unique pair) | FK → `authority.name` |
| `Employee` | `employee` | `empId`, `name`, `email`, `salary`, `joiningDate`, `supervisor` (bool), `employeeType`, `grade` (`EnumType.STRING`), `photoFilename` | `ManyToOne → Department`; `OneToOne → AppUser` (nullable) |
| `Department` | `department` | `deptId`, `departmentCode` (unique), `departmentName`, `status`, `@Version` | referenced by `Employee.department` |
| `UserSession` | `user_sessions` | `sessionId`, `username`, `loginTime`, `logoutTime`, `durationSeconds` | matched by username string |
| `RefreshToken` | `refresh_tokens` | `token` (unique UUID), `username`, `sessionId`, `expiryDate` | — |
| `BlacklistedToken` | `blacklisted_tokens` | `id`, `token` (unique), `expiresAt` | purged hourly |
| `LoginAttempt` | `login_attempts` | `username` (PK), `attemptCount`, `updatedAt` | purged daily |

**Enums:** `DepartmentStatus` (`ACTIVE`/`INACTIVE`), `EmployeeType` (`EMPLOYEE`/`AGENT`), `Grade` (`JUNIOR`/`MID`/`SENIOR`/`LEAD` — stored as `EnumType.STRING`, safely reorderable).

---

## 5. Controller-by-controller reference

### `AuthController` — `/api/auth`
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| POST | `/login` | public | Authenticate, start session, issue tokens + authorities list |
| POST | `/logout` | authenticated | Blacklist token, close session, delete refresh token |
| POST | `/refresh` | public | Exchange refresh token for new access token (preserves `sid` + returns `authorities`) |

### `EmployeeController` — `/api/employees`
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| POST | `/` | `MANAGE_EMPLOYEES` | Create employee |
| GET | `/` | authenticated | List all |
| GET | `/{empId}` | authenticated | Get one |
| PUT | `/{empId}` | `EDIT_ALL_PROFILE` OR own record | Update |
| DELETE | `/{empId}` | `DELETE_EMPLOYEE` | Delete |
| GET | `/meta/types`, `/meta/grades` | authenticated | Enum value lookups |
| GET | `/paged` | authenticated | Paginated list |
| GET | `/stats` | authenticated | Dashboard stats: headcount, supervisors, by-department/grade, salary bands |
| POST | `/{id}/photo` | authenticated | Upload employee photo |
| GET | `/me` | authenticated | The logged-in user's own `Employee` record (404 if none linked) |

### `DepartmentController` — `/api/departments`
| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| POST / PUT / DELETE | | `MANAGE_DEPARTMENTS` | Create / update / delete |
| GET `/`, `/{deptId}`, `/meta/statuses` | authenticated | Reads |

### `UserController` — `/api/users` (class-level `hasRole('ADMIN')`)
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/` | List all login accounts |
| POST | `/` | Create a user + linked employee record. `role` is accepted as a **string role code** and resolved server-side via `RoleRepository.findByRoleCode()` (not a full `Role` object payload) |
| PUT | `/{id}/reset-password` | Admin resets any user's password |
| POST | `/grant/{employeeId}` | Grant login access to an existing employee; `role` also a string code |
| DELETE | `/{id}` | Delete a user account |

### `RoleController` / `AuthorityController` — see §3.2

### `SessionController` — `/api/sessions` (class-level `hasAnyRole('ADMIN','SUPERVISOR')`)
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/stats?days=`, `/summary?days=`, `/daily?days=` | Dashboard aggregate data |
| GET | `/user/{username}` | All sessions for one user |
| GET | `/activity?fromDate=&toDate=` | Per-user totals within a date range |
| GET | `/user/{username}/activity?fromDate=&toDate=` | One user's sessions within a date range |

---

## 6. Service Layer

- **`EmployeeService`** — CRUD + `resolveDepartment()` + `savePhoto()`.
- **`DepartmentService`** — CRUD; blocks deleting a department with assigned employees.
- **`CustomUserDetailsService`** — builds the RBAC principal (§3.4).
- **`TokenBlacklistService`** — DB-backed; `blacklist()` persists with the token's real expiry; `purgeExpired()` runs hourly.
- **`LoginAttemptService`** — DB-backed; one row per username; `purgeStale()` runs daily.

---

## 7. Scheduled Maintenance Jobs

`@EnableScheduling` on `DemoApplication`:

| Job | Frequency | Purpose |
|---|---|---|
| `TokenBlacklistService.purgeExpired()` | Hourly | Deletes blacklist rows past their JWT's natural expiry |
| `LoginAttemptService.purgeStale()` | Daily | Clears lockouts older than 24h |

---

## 8. Error Handling

`exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`) — consistent `{ timestamp, status, error, message }` shape:

| Exception | Status | Triggered by |
|---|---|---|
| `EmployeeNotFoundException` | 404 | Missing employee/department |
| `MethodArgumentNotValidException` | 400 | `@Valid` failures — field errors under key `messages` (corrected from the earlier `massages` typo) |
| `IllegalArgumentException` | 400 | e.g. duplicate role code, unknown authority name, missing department |
| `DepartmentInUseException` | 409 | Deleting a department with assigned employees |
| `AccessDeniedException` | 403 | Any `@PreAuthorize` rejection |

---

## 9. Static file serving

Employee photos: saved to `uploads/employee-photos/`, served at `GET /photos/{filename}`, `permitAll`.

---

## 10. Known Issues / Technical Debt Log

1. **No protection when deleting an in-use or structural role.** `RoleController.deleteRole()` will delete `ADMIN` itself, or any role currently assigned to users, silently orphaning their `role_id`. Add a guard: block if `roleCode = 'ADMIN'`, and/or block if any `AppUser` references the role.
2. **`DaoAuthenticationProvider()` deprecation** — still using the deprecated constructor + setter pattern in `SecurityConfig`.
3. **No automated tests** — `DemoApplicationTests.java` only verifies the Spring context loads. No coverage on RBAC, auth flow, or the role/authority CRUD endpoints.
4. **`RoleController`/`AuthorityController` use `@CrossOrigin(origins = "http://localhost:4200")`** in addition to the global CORS bean in `SecurityConfig` — redundant (harmless today since both list the same origin, but worth consolidating to avoid drift if the frontend origin ever changes).
