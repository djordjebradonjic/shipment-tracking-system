# Shipment Tracking System

A RESTful shipment tracking and management system built with Java 21 and Spring Boot 3.5, featuring JWT authentication, role-based access control, CSV bulk import, and a web UI served directly by the application.

---

## Quick Start

The entire application runs with a single command — no manual configuration required.

```bash
git clone https://github.com/djordjebradonjic/shipment-tracking-system.git
cd shipment-tracking-system
docker compose up --build
```

Once started, open **http://localhost:8080** in your browser.

---

## Test Credentials

Two roles are available. Register new accounts at **http://localhost:8080/register.html**
Example
| Role | Email | Password |
|---|---|---|
| Employee | employee@test.com | password |
| Customer | customer@test.com | password |

**Role differences:**
- **EMPLOYEE** — full access: manage users, create shipments for any customer, change shipment status, import CSV, view all shipments
- **CUSTOMER** — limited access: create their own shipments, view and track their own shipments only

---

## Features

### Core Functionality
- **Shipment lifecycle management** — create shipments and track them through statuses: `CREATED → IN_TRANSIT → DELIVERED` or `CANCELLED`
- **Status state machine** — enforces valid transitions, rejects invalid ones with `409 Conflict`
- **Status history audit trail** — every status change is recorded with timestamp and optional note
- **User management** — create and manage customers

### Filtering, Sorting & Pagination
- Filter shipments by user, status, and date range
- Server-side pagination with configurable page size (10/20/50)
- Sort by creation date, tracking number, or status

### CSV Bulk Import
- Import shipments from CSV with automatic user creation (find-or-create by email)
- Optional status column — imported shipments can have any valid initial status
- All-or-nothing transaction strategy — if any row fails, nothing is saved

CSV format:
```
email,firstName,lastName,phone,description,origin,destination,weightKg,status
customer@example.com,John,Doe,0601234567,Laptop,Novi Sad,Beograd,2.1,
another@example.com,Jane,Smith,0639876543,Books,Beograd,Nis,1.5,IN_TRANSIT
```

Sample CSV files are available in the `sample-data/` folder.

### Authentication & Security
- JWT-based authentication (stateless, no server-side sessions)
- Role-based access control enforced on both backend and frontend
- Token expiry: 24 hours

### Web UI
- Login and registration pages
- Role-aware interface — employees see full management tools, customers see only their shipments
- Shipment history modal showing full audit trail per shipment

### API Documentation
Swagger UI is available at **http://localhost:8080/swagger-ui.html** without authentication.

### Using Swagger UI with Authentication

Some endpoints require authentication. To authorize in Swagger UI:

1. Register an account at `POST /api/v1/auth/register` or use the test credentials above
2. Login at `POST /api/v1/auth/login` — copy the `token` from the response
3. Click the **Authorize** button (top right in Swagger UI)
4. Enter: `Bearer <your-token>` and click Authorize
5. All subsequent requests will include the token automatically
---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.15 |
| Database | PostgreSQL 16 |
| Schema migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Object mapping | MapStruct 1.6.3 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| CSV parsing | OpenCSV 5.12.0 |
| API docs | springdoc-openapi (Swagger UI) |
| Build tool | Maven |
| Containerization | Docker + Docker Compose |
| Frontend | Vanilla JS (served by Spring Boot) |

---

## Architecture

```
controller/   — HTTP layer, request/response handling
service/      — Business logic, transactions
repository/   — Spring Data JPA interfaces
model/        — JPA entities
dto/          — Request and response DTOs
mapper/       — MapStruct entity ↔ DTO mappings
security/     — JWT filter, UserDetails, SecurityConfig
exception/    — Custom exceptions + GlobalExceptionHandler
```

**Key design decisions:**

- **Layered architecture** — strict separation between HTTP, business logic, and data access layers
- **Constructor injection everywhere** — via Lombok `@RequiredArgsConstructor`, never field injection
- **DTOs always, entities never** — controllers never expose JPA entities directly
- **Flyway over ddl-auto:update** — versioned, auditable schema changes
- **EnumType.STRING** — enum values stored as strings, safe against reordering
- **open-in-view: false** — forces explicit JOIN FETCH awareness, prevents hidden N+1 queries
- **JOIN FETCH via Specification** — prevents N+1 when loading shipments with owner data
- **All-or-Nothing CSV import** — `@Transactional` rollback guarantees atomicity; row-by-row processing stops at first error

---

## Running Without Docker (Development)

**Prerequisites:** Java 21, Maven, Docker (for database only)

**1. Start only the database:**
```bash
docker compose up -d db
```

**2. Configure IntelliJ Run Configuration — Environment Variables:**
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5435/shipments
SPRING_DATASOURCE_USERNAME=shipments_user
SPRING_DATASOURCE_PASSWORD=change_me
```

**3. Run** `ShipmentTrackingSystemApplication` from IntelliJ.

Application starts at **http://localhost:8080**.

---

## API Overview

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login, returns JWT token |

### Users (EMPLOYEE only)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/users` | Create user |
| GET | `/api/v1/users` | List all users |
| GET | `/api/v1/users/{id}` | Get user by ID |

### Shipments
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/shipments` | Any | Create shipment |
| GET | `/api/v1/shipments` | Any | Search/filter shipments |
| GET | `/api/v1/shipments/{id}` | Any | Get shipment by ID |
| PATCH | `/api/v1/shipments/{id}/status` | EMPLOYEE | Update status |
| GET | `/api/v1/shipments/{id}/history` | Any | Get status history |
| POST | `/api/v1/shipments/import` | EMPLOYEE | Import from CSV |

**Filtering parameters for `GET /api/v1/shipments`:**
```
?userId=1&status=IN_TRANSIT&createdFrom=2026-01-01T00:00:00Z&createdTo=2026-12-31T23:59:59Z&page=0&size=20&sort=createdAt,desc
```

---

## Testing

Run all tests:
```bash
./mvnw test
```

**Test coverage:**
| Test class | Type | What it covers |
|---|---|---|
| `ShipmentStatusTransitionValidatorTest` | Unit | All 16 status transition combinations (4 valid, 12 invalid) |
| `TrackingNumberGeneratorTest` | Unit | Format, uniqueness, uppercase suffix |
| `ShipmentImportServiceIntegrationTest` | Integration | Happy path CSV import, exception type |
| `ShipmentImportRollbackTest` | Integration | All-or-Nothing rollback — verifies nothing is saved on error |
| `UserControllerTest` | Slice (@WebMvcTest) | Bean Validation → 400 on missing required fields |

**Note:** Integration tests require the database to be running (`docker compose up -d db`).

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/shipment_tracking_system/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── mapper/
│   │   ├── security/
│   │   └── exception/
│   └── resources/
│       ├── db/migration/
│       │   ├── V1__init_schema.sql
│       │   └── V2__add_auth_columns.sql
│       ├── static/
│       │   ├── index.html
│       │   ├── login.html
│       │   └── register.html
│       └── application.yaml
├── test/
│   ├── java/
│   └── resources/
│       └── application.yaml  ← uses localhost:5435 instead of db:5432
└── sample-data/
    ├── shipments-import-example.csv
    └── shipments-import-example-with-error.csv
```

---

