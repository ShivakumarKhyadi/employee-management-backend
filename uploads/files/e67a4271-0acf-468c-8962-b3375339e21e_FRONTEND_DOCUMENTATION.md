# Employee Management System — Frontend Documentation

**Stack:** Angular (standalone components, **zoneless** change detection — no `zone.js`) · Chart.js · RxJS
**API base:** `http://localhost:8080` (hardcoded per-service)
**Auth storage:** `sessionStorage` (JWT, refresh token, username, role, **authorities array**) · `localStorage` (theme preference)

---

## ⚠️ Build-breaking bug — fix before anything else

**`components/user-list/user-list.ts` currently has a duplicate field declaration:**
```typescript
export class UserList implements OnInit {
  users: AppUser[] = [];
  departments: Department[] = [];
  roles: Role[] = [];              // ← first declaration
  grades: Grade[] = ['JUNIOR', 'MID', 'SENIOR', 'LEAD'];

  roles: Role[] = [];               // ← DUPLICATE — TS2300: Duplicate identifier 'roles'
  ...
```
This is a TypeScript compile error — the app will not build until the second `roles: Role[] = [];` line is deleted. Delete the duplicate (keep whichever one; they're identical) before doing anything else with this codebase.

---

## 1. Architecture at a Glance

```
src/app
├── app.ts / app.html / app.routes.ts / app.config.ts   Root shell: header/nav/footer, route table, providers
├── home/                                                 Post-login landing page
├── profile-modal/                                        "My Profile" popup (avatar click)
├── role-list/, role-create/, role-detail/                Roles & Authorities admin pages (top-level, not under components/)
├── components/
│   ├── login/                    Login form
│   ├── employee-list/            Main roster page ("/employees")
│   ├── employee-form/            Add/edit employee
│   ├── employee-profile/         Single employee detail page
│   ├── department-list/          Department roster
│   ├── department-form/          Add/edit department
│   ├── user-list/                Admin: manage login accounts ("/users")
│   ├── dashboard/                 Stats + activity charts ("/dashboard")
│   ├── activity-log/              Per-user activity drill-down ("/activity/:username")
│   ├── confirm-modal/             Reusable delete-confirmation dialog
│   ├── toast-container/           Global toast notifications
│   └── not-found/                 404 page
├── services/                      HTTP clients + app-wide state
├── guards/                        Route access control
├── interceptors/                  Auto-attach JWT, handle 401 refresh-and-retry
└── utils/                          Pure helper functions
```

Note the inconsistent placement: `role-list/`, `role-create/`, `role-detail/`, `home/`, and `profile-modal/` all live directly under `src/app/`, while every other page lives under `src/app/components/`. Not a functional bug, just worth normalizing for consistency if you're doing further cleanup.

**Zoneless change detection:** this app has no `zone.js`. HTTP responses inside `.subscribe()` do **not** automatically trigger a re-render — every component that mutates state in an async callback must call `this.cdr.markForCheck()` afterward, or the UI silently goes stale until an unrelated DOM event forces a re-check. This has been the root cause of multiple bugs across the project (`NG0100` errors, "stuck on loading" states, a profile modal showing stale data). Keep this in mind for every new async handler.

---

## 2. Routing (`app.routes.ts`)

| Path | Component | Guard |
|---|---|---|
| `/login` | `Login` | — |
| `/` | `Home` | `authGuard` |
| `/employees` | `EmployeeList` | `authGuard` |
| `/roles` | `RoleList` | **none** |
| `/roles/new` | `RoleCreate` | **none** |
| `/roles/:roleCode` | `RoleDetail` | **none** |
| `/dashboard` | `Dashboard` | `authGuard` **(duplicate entry, see Known Issues #1)** |
| `/dashboard` | `Dashboard` | `elevatedGuard` **(shadowed — never reached)** |
| `/add`, `/edit/:id` | `EmployeeForm` | `authGuard` |
| `/employees/:id` | `EmployeeProfile` | `authGuard` |
| `/departments`, `/departments/add`, `/departments/edit/:id` | `DepartmentList`/`DepartmentForm` | `authGuard` |
| `/users` | `UserList` | `adminGuard` |
| `/activity/:username` | `ActivityLog` | `adminGuard` |
| `**` | `NotFound` | — |

### Guards (`guards/`)
| Guard | Passes when | Redirects to |
|---|---|---|
| `authGuard` | `authService.isLoggedIn()` | `/login` |
| `adminGuard` | logged in **and** `isAdmin()` | `/` |
| `elevatedGuard` | `authService.canManage()` | `/` |

**No guard at all on `/roles`, `/roles/new`, `/roles/:roleCode`.** The backend correctly rejects non-Admins with a `403` (`RoleController`/`AuthorityController` are `hasRole('ADMIN')`), so there's no actual security hole — but a non-Admin who navigates there will see broken/empty pages and raw error states instead of being redirected like every other Admin-only page. Should use `adminGuard`, matching `/users`.

---

## 3. Root Shell (`app.ts` / `app.html`)

- Sticky header with logo, nav links, dark-mode toggle, profile avatar, logout — shown only when logged in and not on `/login`.
- "Dashboard" nav link: `*ngIf="authService.canManage()"`. "Users" nav link: `*ngIf="authService.isAdmin()"`. (No "Roles" nav link condition confirmed in this pass — verify it's gated the same way as "Users" if not already.)
- Watches `router.events` for `NavigationEnd` to start/stop `IdleService`'s auto-logout timer.
- Hosts `<app-toast-container>`, the logout-confirmation modal, and `<app-profile-modal>` (re-fetches `/api/employees/me` fresh every time it opens — deliberately not cached, to avoid showing a previous user's data after a logout/login cycle).

---

## 4. Authorization model on the frontend (`auth.service.ts`)

This is now **authority-driven**, not role-name-driven — a meaningful architecture change from earlier in the project:

```typescript
interface LoginResponse {
  token: string;
  username: string;
  role: string;
  authorities: string[];
}
```

- `login()` stores the full `authorities` array (JSON-stringified) in `sessionStorage` alongside username/role/tokens.
- `hasAuthority(name: string): boolean` — the new primitive everything else is built on.
- `isAdmin()` → `hasAuthority('ROLE_ADMIN')`
- `isSupervisor()` → `hasAuthority('ROLE_SUPERVISOR')`
- `canManage()` → `hasAuthority('VIEW_DASHBOARD')`

**Practical effect:** if an Admin creates a brand-new role (e.g. `TEAM_LEAD`) via the Roles UI and attaches `VIEW_DASHBOARD` to it, any user with that role automatically sees the Dashboard nav link and passes `elevatedGuard` — with zero frontend code changes. The permission model is now genuinely data-driven end-to-end (backend AND frontend), not just backend.

**Caveat:** `isAdmin()`/`isSupervisor()` now mean "has the `ROLE_ADMIN`/`ROLE_SUPERVISOR` authority," not "role name is literally ADMIN/SUPERVISOR." This is more flexible (a second role could theoretically be granted `ROLE_ADMIN` for admin-equivalent access) but is a subtle behavior shift from the original design — worth being aware of if debugging "why does this user have admin nav links."

**Client-side JWT expiry check** (`isTokenExpired`) decodes the payload without verifying the signature — fine as a UX optimization, never a security boundary; the backend remains the actual enforcement point.

---

## 5. Roles & Authorities admin UI

Three pages, all currently ungated by a route guard (see §2):

### `role-list/` — `/roles`
- Table of all roles (code, display name, View / Manage authorities / Delete actions).
- Delete goes through `<app-confirm-modal>` (not an instant delete).
- Inline "+ New authority" form on the same page (kept deliberately merged with roles, not split into a separate page, per product decision).
- "+ Add role" navigates to `/roles/new`.

### `role-create/` — `/roles/new`
- Form: role code, display name, and a type-to-search authority picker (chips for selected authorities, click-to-remove).
- Submits via `RoleService.createRole()`, which hits the transactional `POST /api/roles` — a bad authority name rolls back the entire creation server-side.

### `role-detail/` — `/roles/:roleCode`
- Two modes on the same route, toggled via `?view=true` query param: **View mode** (read-only) and **Manage authorities** (default — add/remove authorities via the same search-and-click picker, each attach/detach is its own API call, not a batch save).
- Uses reactive `route.paramMap`/`route.queryParamMap` subscriptions (not one-time snapshots) — necessary because Angular reuses this component instance when only the query param changes between the two modes, so a snapshot-based read would never update after the initial load.

### `services/role.service.ts` / `services/role.ts`
Full CRUD client for `/api/roles` and `/api/authorities`, plus role↔authority attach/detach. `Role`, `Authority`, `RoleAuthority`, `CreateRoleRequest`, `CreateAuthorityRequest` types defined in `role.ts`.

---

## 6. Services Reference

| Service | Base URL | Responsibility |
|---|---|---|
| `AuthService` | `/api/auth` | Login/logout/refresh, session storage, authority-driven role helpers (§4) |
| `EmployeeService` | `/api/employees` | CRUD, meta lookups, photo upload, `getMyProfile()` |
| `DepartmentService` | `/api/departments` | CRUD, status meta lookup |
| `UserService` | `/api/users` | Admin account management |
| `RoleService` | `/api/roles`, `/api/authorities` | Role & authority CRUD, attach/detach (§5) |
| `SessionService` | `/api/sessions` | Dashboard/activity data |
| `ThemeService` | — | Dark-mode toggle (`signal`-based), persists to `localStorage` |
| `ToastService` | — | Global toast queue |
| `IdleService` | — | Auto-logout after 3 min inactivity; listens outside Angular's zone for performance |

### Role dropdowns are now dynamic
`employee-list.ts` (Grant Login modal) and `user-list.ts` (Add User form) both load `RoleService.getAllRoles()` and render `*ngFor` options instead of hardcoded `USER`/`ADMIN`/`SUPERVISOR` literals — so any custom role created via `/roles/new` immediately becomes assignable to users without a frontend code change.

**Leftover from before this fix:** `employee-list.ts` still types `grantRole: 'ADMIN' | 'USER' = 'USER';` — a stale TypeScript union that doesn't include `'SUPERVISOR'` or any custom role code. Since the `<select>` binds via `[(ngModel)]` to this field and the template now offers arbitrary role codes from the API, this type is inaccurate and should be widened to `string`.

---

## 7. Component Reference (selected — see prior documentation pass for full detail on Employee/Department/User/Dashboard/ActivityLog pages, all unchanged in this pass except where noted)

### `Home` (`home/`)
Minimal welcome page, "Hello, `{{ username }}`" — landing page after login.

### `ProfileModal` (`profile-modal/`)
Fetches `/api/employees/me` fresh on every open (not cached). Falls back to username/role display if no linked employee record. Uses `ChangeDetectorRef.markForCheck()` in both success/error branches (zoneless-safe).

### `Dashboard` (`components/dashboard/`)
Employee stats (headcount, salary distribution — gated `canManage()`) + session activity charts (minutes per user, share of time, logins over time — gated `canManage()`, auto-refreshes every 60s). Uses **preset range buttons** (Today/7 days/30 days/All time), not an explicit From/To picker (that only exists on `ActivityLog`).

### `ActivityLog` (`components/activity-log/`)
Per-user drill-down with an explicit From/To date range picker and a per-day bar chart, reached by clicking a bar in Dashboard's "Minutes per user" chart.

---

## 8. Known Issues / Technical Debt Log

1. **Build-breaking duplicate `roles` field in `user-list.ts`** — see the top of this document. Fix before anything else.
2. **Duplicate `/dashboard` route entry** in `app.routes.ts` — the first (`authGuard`-only) matches before the second (`elevatedGuard`) ever gets a chance, so the intended Supervisor/Admin-only gating at the routing layer isn't actually enforced (the backend still blocks the underlying API calls, so this isn't a data leak — a plain User just sees a mostly-empty dashboard shell). Delete the first entry.
3. **`/roles`, `/roles/new`, `/roles/:roleCode` have no route guard** — should use `adminGuard`, matching `/users`.
4. **`employee-list.ts`'s `grantRole` field type is stale** (`'ADMIN' | 'USER'`) — doesn't reflect that the dropdown now offers Supervisor and any custom role. Widen to `string`.
5. **Inconsistent folder structure** — `role-list/`, `role-create/`, `role-detail/`, `home/`, `profile-modal/` live at `src/app/` top level while everything else is under `src/app/components/`. Cosmetic, but worth normalizing.
6. **Hardcoded `http://localhost:8080` in every service** — no `environment.ts`-based configuration; needs externalizing before any real deployment.
7. **Zoneless change detection** (see §1) remains the most common source of subtle bugs — any new `.subscribe()` needs `markForCheck()` after mutating state.
8. **`RoleController`/`AuthorityController` delete/attach/detach actions have no confirmation UX equivalent to employee/department/user deletes for authority *removal* specifically** — role deletion does use `<app-confirm-modal>`, but removing a single authority from a role (`role-detail`'s "Remove" button) fires immediately with no confirmation step. Worth deciding if that's intentional (low-stakes, reversible via re-add) or should match the same confirm pattern.
