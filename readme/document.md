# DEIConnect — Project Documentation

**Diversity, Equity & Inclusion management platform**
Angular 18 single-page app + Spring Boot 3 / Java 21 microservices, behind an API gateway with
service discovery, stateless JWT auth, method-level RBAC, and privacy/anonymity guardrails enforced
in the service layer.

> This document covers the **whole project** (frontend + all backend services). It reflects the
> system as it actually runs and was verified end-to-end through the gateway.

---

## 1. What it does

DEIConnect manages the full DEI lifecycle for an organisation while treating **sensitive demographic
and pay data** as a first-class privacy concern:

- **Demographic self-ID** — employees voluntarily record gender/ethnicity/etc. with explicit consent.
- **Representation snapshots** — managers aggregate consented profiles into anonymised, threshold-protected metrics.
- **DEI goals** — multi-year targets with quarterly progress tracking and trend/gap computation.
- **Pay equity** — HR runs gap analyses (compute from workforce or manual flags), publishes aggregates.
- **Inclusion surveys** — anonymous surveys scored into aggregate inclusion indices.
- **ERGs** — Employee Resource Groups with memberships and events.
- **Reporting** — a cross-service DEI dashboard that fans out to every domain.
- **Notifications & audit** — in-app notifications and an admin-only audit trail.

---

## 2. Architecture

Distributed system: an Angular SPA talks **only** to the API gateway, which routes to three backend
services registered in Eureka. Two of those services call back into the core service via Feign.

```
                         ┌─────────────────────────┐
  Browser (Angular SPA)  │  http://localhost:4200  │
                         └───────────┬─────────────┘
                                     │  JWT (Bearer)  + CORS
                                     ▼
                         ┌─────────────────────────┐
     API Gateway         │  http://localhost:8060  │  Spring Cloud Gateway (WebFlux)
                         │  JwtValidation filter    │  validates JWT, stamps identity headers
                         └───────────┬─────────────┘
                 lb:// (Eureka-resolved routing)
        ┌────────────────────────────┼──────────────────────────────┐
        ▼                            ▼                                ▼
┌───────────────────┐   ┌────────────────────────┐    ┌────────────────────────┐
│ dei-connect :7000 │   │ survey-service   :8081 │    │ erg-service     :8082  │
│ (core "monolith") │   │ inclusion surveys      │    │ ERGs / events          │
│ iam · diversity · │   │                        │    │                        │
│ goal · payequity ·│◄──┤ Feign UserClient       │    │                        │
│ reporting · notif │   └────────────────────────┘    └────────────────────────┘
│ · audit           │◄── Feign SurveyClient / ErgClient (reporting fan-out) ──┘
└─────────┬─────────┘
          │        Eureka discovery: http://localhost:8761
          ▼
   MySQL :3306  →  schemas: deiconnect · deiconnect_survey · deiconnect_erg
```

### Services & ports

| Service | Port | Role |
|---|---|---|
| `eureka-server` | 8761 | Service discovery (must start first) |
| `api-gateway` | 8060 | **Sole external entry point**; JWT validation + identity propagation + CORS |
| `dei-connect` | 7000 | Core service: IAM, diversity, goals, pay-equity, reporting, notifications, audit |
| `survey-service` | 8081 | Inclusion surveys, questions, responses, summaries |
| `erg-service` | 8082 | ERGs, memberships, events, participation |
| Angular dev server | 4200 | Frontend (`ng serve`) |
| MySQL | 3306 | 3 auto-created schemas (root/root) |

### Why microservices (and how identity flows)
- **Independent data ownership** — each service owns its own MySQL schema; no cross-schema joins.
- **Independent scale & blast radius** — a survey burst scales `survey-service` alone; an ERG outage doesn't touch auth/pay.
- **Identity propagation** — the gateway validates the JWT once (`JwtValidation` filter) and forwards
  `X-User-Id`, `X-User-Role`, `X-User-Email`, `X-User-EmployeeId`, plus a shared `X-Internal-Auth`
  secret. Downstream services (`survey`, `erg`) trust those headers **only when the secret matches**
  (`HeaderAuthenticationFilter`), so a caller hitting a service directly cannot forge a role.
- **Cross-service calls (Feign, with fallbacks):**
  - `survey-service` → `dei-connect` `UserClient` for a responder's dept/grade/manager/HR (summary bucketing).
  - `dei-connect` reporting → `survey-service` `SurveyClient` (inclusion index) and `erg-service` `ErgClient` (ERG membership rate).

---

## 3. Tech stack

| Layer | Choice |
|---|---|
| Frontend | Angular 18 (standalone components, signals, reactive forms), TypeScript, Bootstrap 5 / bootstrap-icons |
| Backend | Java 21, Spring Boot 3.2.x |
| Gateway | Spring Cloud Gateway (WebFlux) |
| Discovery | Spring Cloud Netflix Eureka |
| Inter-service | OpenFeign (with fallbacks) |
| Persistence | Spring Data JPA / Hibernate → MySQL (portable to PostgreSQL/SQL Server) |
| Security | Spring Security, stateless JWT (HS256, `io.jsonwebtoken`), method-level `@PreAuthorize` RBAC |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok |

---

## 4. Security model

- **Stateless JWT.** `POST /api/auth/login` issues an HS256 token with claims `sub` (email),
  `uid`, `eid` (employeeId), `role`. No server session; each request is authorized from the token.
  Expiry is 24h (`app.jwt.expiration-ms=86400000`). Logout is **client-side only** — the token is
  discarded but not revoked server-side (valid until it expires).
- **Two-tier authorization.** The gateway validates the JWT; each service enforces
  `@PreAuthorize("hasRole/hasAnyRole/isAuthenticated")` at the method level and adds **row-level
  ownership** checks in the service layer where a role gate isn't enough.
- **Authorities** are `ROLE_<RoleName>` derived from the JWT `role` claim.
- **Public endpoints:** `/api/auth/**`, health, and OpenAPI/Swagger. Everything else needs a JWT.
- **No self-registration** — accounts are ADMIN-provisioned. A default admin is seeded on first boot.

### Roles

| Role | Responsibilities |
|---|---|
| `EMPLOYEE` | Own self-ID data; take surveys anonymously; join/leave ERGs & events; own profile & notifications |
| `DEI_MANAGER` | Create/track DEI goals; generate & publish representation snapshots; create ERGs; author survey questions |
| `HR_BIZ_PARTNER` | Run/compute/publish pay-equity analyses; manage pay-gap flags & remediation (raw pay restricted to this role + ADMIN) |
| `ERG_LEAD` | Govern own ERG chapter: memberships, events, participation |
| `EXECUTIVE` | Read-only consumer of aggregated, suppressed dashboards; ERG sponsor |
| `ADMIN` | Manage users; create/publish reports & surveys; emit notifications; **sole audit-log reader** |

The role enum is identical on frontend (`core/models/enums.ts`) and backend (`common/enums/Role.java`)
and travels over the wire as its name.

---

## 5. Privacy & anonymity guardrails (service layer)

| ID | Guardrail | Enforcement |
|---|---|---|
| **A** | **Minimum group size / k-anonymity** | Disaggregated demographic/survey/pay metrics are checked against `app.privacy.default-min-group-size` (or a survey's `minResponseThreshold`). Below threshold → group **suppressed** (numbers nulled, `suppressed:true`) or never persisted. Publish of a sub-threshold group → **HTTP 422**. |
| **B** | **Anonymised responses** | Survey submissions fold directly into running aggregates. **No individual-response table**; open text never persisted; submit returns only an acknowledgement. |
| **C** | **Consent enforcement** | Demographic data is aggregated only where `consentStatus = CONSENTED`; Declined/Pending are excluded in SQL. |
| **D** | **Audit logging** | Every sensitive read/write records an `AuditLog` entry via `AuditLogWriter`. |
| **E** | **Least privilege** | Raw demographic/pay rows are reachable only by the narrowest roles via **physically separate endpoints** (not post-filtering); broader roles get aggregated, threshold-checked views. There is deliberately no endpoint exposing an individual's raw demographic profile to managers/executives. |
| **F** | **Ownership scoping** | EMPLOYEE acts only on their own self-ID/membership/participation/notifications; ERG_LEAD only on their own chapter; HR only on their own analyses. Enforced via `SecurityUtils` ownership checks. |

Threshold behaviour is layered: **(A-generate)** small groups dropped at generation, **(A-read)** masked
again on every read (so raising the threshold retroactively hides old data), **(A-publish)** blocked with 422.

---

## 6. API conventions

- **Base:** everything under `/api`, reached through the gateway (`http://localhost:8060`).
- **Auth:** `Authorization: Bearer <jwt>` on every endpoint except `/api/auth/**`.
- **Pagination:** Spring `Pageable` — `?page=0&size=20&sort=field,desc`.
- **Enums:** string enums sent/received by name.
- **Verbs:** `POST` create, `GET` read, `PUT` update/state-transition, `DELETE` remove.
- **Errors** (uniform shape):
  ```json
  { "timestamp": "...", "status": 422, "error": "Unprocessable Entity",
    "message": "Cannot publish a snapshot whose group size is below the minimum threshold",
    "path": "/api/representation-snapshots/5/publish" }
  ```
  Common mappings: 400 validation · 401 no/expired token · 403 forbidden (RBAC/ownership) ·
  404 not found · 409 conflict (duplicate/immutable/wrong-state) · 422 privacy-threshold violation.

---

## 7. Feature modules & endpoints

Roles shown are the `@PreAuthorize` gate. All paths are under the gateway.

### Auth — `/api/auth` (dei-connect)
| Method | Path | Role |
|---|---|---|
| POST | `/login` | public |

### Users / IAM — `/api/users` (dei-connect)
| Method | Path | Role |
|---|---|---|
| POST | `/` | ADMIN |
| GET | `/` | DEI_MANAGER, HR, ERG_LEAD, EXECUTIVE, ADMIN |
| GET / PUT | `/me` | any authenticated (self-service profile) |
| PUT / DELETE | `/{id}` | ADMIN |
| GET | `/scope-values` | any authenticated (report scope pickers) |
| GET/POST | `/internal/**` | internal (Feign, service-to-service) |

An EMPLOYEE requires a `managerId` (DEI_MANAGER) and `hrId` (HR_BIZ_PARTNER); the password is set by the
admin (no auto-generation). Duplicate email/employeeId → 409.

### Demographic profile — `/api/demographic-profiles` (dei-connect)
`POST /`, `GET /me`, `PUT /me` — **EMPLOYEE**, owner-scoped. One profile per employee (409 on dupe).
No endpoint exposes a raw profile to any other role (guardrail E).

### Representation snapshots — `/api/representation-snapshots` (dei-connect)
`POST /generate` (DEI_MANAGER), `GET` / `GET /{id}` (DEI_MANAGER, EXECUTIVE), `PUT /{id}/publish` (DEI_MANAGER).
Aggregates CONSENTED profiles by dimension; suppresses sub-threshold groups (A); managers see only their own snapshots.

### DEI goals — `/api/goals` (dei-connect)
Goals: `POST`, `PUT /{id}` (DEI_MANAGER); `GET`, `GET /{id}` (DEI_MANAGER, EXECUTIVE, ADMIN).
Progress `/api/goals/{goalId}/progress`: `POST`, `PUT /{pid}`, `PUT /{pid}/confirm` (DEI_MANAGER); `GET` (+EXECUTIVE, ADMIN).
`gapToTarget` and direction-aware `trend` are computed server-side; confirmed entries are immutable (409).

### Pay equity — `/api/pay-equity` (dei-connect)
Raw surface (HR owns their analyses): `POST/PUT /analyses`, `PUT /analyses/{id}/publish`,
`POST/PUT /analyses/{id}/flags` (HR); `POST /analyses/{id}/compute`, `GET` reads, `GET /flags` (HR, ADMIN).
Published surface: `GET /published/analyses[...]` (DEI_MANAGER, EXECUTIVE, HR, ADMIN).
`computeFromWorkforce` groups CONSENTED employees by dimension, gaps vs the highest-paid group,
experience-adjusted, small groups suppressed; publishing/reads mask sub-threshold flags.

### Inclusion surveys — `/api/surveys` (**survey-service**)
Surveys: `POST`, `PUT /{id}`, `DELETE /{id}`, `PUT /{id}/publish` (ADMIN); `PUT /{id}/launch`, `/{id}/close`
(DEI_MANAGER, ADMIN); `GET`, `GET /{id}` (EMPLOYEE, DEI_MANAGER, EXECUTIVE, ADMIN).
Questions `/{surveyId}/questions`: `GET` (all above); `POST/PUT/DELETE` (DEI_MANAGER, ADMIN).
Responses `/{surveyId}`: `POST /responses` (EMPLOYEE); `GET /summaries` (DEI_MANAGER, EXECUTIVE, ADMIN);
`PUT /summaries/{id}/publish` (ADMIN). Anonymous: only aggregates stored; one submission per employee (409);
summaries suppressed below `minResponseThreshold` and publish blocked (422).

### ERGs — `/api/ergs` (**erg-service**)
Groups: `POST`, `DELETE /{id}` (DEI_MANAGER, ADMIN); `PUT /{id}` (DEI_MANAGER, ADMIN, ERG_LEAD); `GET`, `GET /{id}` (all roles).
Memberships `/{ergId}/memberships`: `POST` join, `DELETE /me`, `GET /me` (EMPLOYEE); `GET` list
(DEI_MANAGER, ERG_LEAD, EXECUTIVE, ADMIN); `PUT /{membershipId}` (ERG_LEAD).
Events `/{ergId}/events`: `POST/PUT/DELETE` (ERG_LEAD); `GET`, `GET /{eventId}` (all roles);
`POST/DELETE /{eventId}/participate` (EMPLOYEE); `GET /{eventId}/participants` (DEI_MANAGER, ERG_LEAD, ADMIN).
Duplicate join/participate → 409.

### Reporting — `/api/reports` (dei-connect)
`POST`, `PUT /{id}`, `PUT /{id}/publish`, `DELETE /{id}` (ADMIN); `GET`, `GET /{id}`, `GET /{id}/data`
(DEI_MANAGER, HR, EXECUTIVE, ADMIN). A report is a **definition** (scope + metrics); `GET /{id}/data`
computes on demand and **fans out** per metric — representation/pay-equity/goals in-process,
`INCLUSION_INDEX` via Feign→survey-service, `ERG_MEMBERSHIP_RATE` via Feign→erg-service (fallbacks keep
the dashboard rendering if a service is down). Non-admins see PUBLISHED reports only.

### Notifications — `/api/notifications` (dei-connect)
`GET`, `GET /unread-count`, `GET /{id}`, `PUT /{id}/read`, `PUT /{id}/dismiss`, `PUT /read-all`
(any authenticated, owner-scoped); `POST /emit` (ADMIN). Also emitted internally (e.g. publishing a
pay-equity analysis notifies managers/HR/exec/admin).

### Audit logs — `/api/audit-logs` (dei-connect)
`GET` (ADMIN only), filterable by `userId` and `entityType`.

---

## 8. Frontend

Angular 18 standalone-component app. Talks only to the gateway (`environment.apiBaseUrl = http://localhost:8060`).

### Structure (`dei_frontend/src/app`)
```
core/                      cross-cutting plumbing (singletons)
  auth/                    AuthService (session + signals), jwt.util
  constants/               api-paths, nav-config, labels
  guards/                  authGuard, loginGuard, roleGuard
  interceptors/            authInterceptor, loadingInterceptor, errorInterceptor
  models/                  DTO interfaces + enums (field names mirror backend JSON)
  services/                one HTTP service per domain (user, diversity, goal, pay-equity, survey, erg, report, notification, audit, ...)
shared/                    reusable UI (navbar, sidebar, paginator, page-header, status-badge, ...),
                           directives (*appHasRole), pipes (enumLabel, roleLabel), layouts (auth-layout, main-layout)
features/                  one folder per domain, lazy-loaded:
  auth/ (login, forgot-password) · errors/ (access-denied, not-found) · dashboard/ (role dashboards)
  users/ · profile/ · diversity/ · goals/ · pay-equity/ · surveys/ · ergs/ · notifications/ · reports/ · audit/
```

### Request & session flow
1. `LoginComponent` → `AuthService.login()` → `POST /api/auth/login`.
2. `persistSession()` stores the JWT in memory + `localStorage['dei.token']` and the user in
   `localStorage['dei.user']`; reactive `currentUser` / `isAuthenticated` / `role` signals update.
3. `authInterceptor` attaches `Authorization: Bearer <token>` to every request except login.
4. `authGuard` protects the authenticated shell (`MainLayoutComponent`); `roleGuard` gates routes by
   `data.roles`; the `*appHasRole` directive hides UI a role can't use (**UX only — the backend is the real gate**).
5. `errorInterceptor` is the backstop: 401 → logout + `/login`; others → toast.
6. On startup the session rehydrates from localStorage and is discarded if the token is expired.

Role-based dashboards are chosen by `DashboardHostComponent` via `@switch (auth.role())`.

---

## 9. Running the project

**Prerequisites:** JDK 21, Maven (or the IDE-bundled one), Node 20+/npm, MySQL on `localhost:3306`
(user `root` / pass `root` — the three schemas auto-create on first boot).

**Backend (all 5 services, correct order):**
```powershell
cd readme
.\start-backend.ps1            # or .\start-backend.ps1 -Build to build first
.\stop-backend.ps1             # stops all 5 by port
```
Order: eureka (8761) → dei-connect (7000) + survey (8081) + erg (8082) → api-gateway (8060, last).
Each service can also be run alone: `mvn -pl <module> spring-boot:run` from `dei_backend`.

**Frontend:**
```powershell
cd dei_frontend
npm install      # first time
npm start        # ng serve → http://localhost:4200
```

**Seeded admin (created on dei-connect's first boot if absent):**
- Email: `adm001@deiconnect.com`
- Password: `Welcome@001`
- All other users are created by an admin via **Users → New**.

**Config of note** (`dei-connect/src/main/resources/application.properties`):
- `app.jwt.secret`, `app.jwt.expiration-ms=86400000`
- `app.privacy.default-min-group-size` — the k-anonymity threshold (raise it to see suppression).

---

## 10. Constraints (current phase)

- **No self-registration / password reset** — accounts are admin-managed; the `/forgot-password` page is a
  static "contact your administrator" notice (no backend endpoint).
- **JWT is stateless** — logout clears the client only; a token stays valid server-side until it expires.
- **Notifications are in-app only** (no email/SMS).
- **No HRIS/payroll integration** — demographic and pay data are self-declared / internally entered.
- Pay-equity metrics are computed server-side from workforce data or manual flags (no external stats engine).

---

## 11. Verification status

Every feature has been exercised end-to-end through the gateway (the exact path the browser uses),
across all three microservices, including: auth & RBAC, user CRUD, demographic profiles, representation
snapshots + privacy suppression/422, goals + progress trend/immutability, pay-equity compute/flags/publish,
surveys lifecycle + anonymity, ERGs groups/events/memberships, reporting cross-service fan-out,
notifications, audit logs, and self-service profile. Guardrails A–F and the role matrix were confirmed
firing (403/409/422/400 as designed).
