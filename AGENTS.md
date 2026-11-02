# Agent Instructions & Project Context: Multi-Tenant College Management System

## 1. Project Overview & Tech Stack
- **Application Type:** Multi-tenant College ERP System packaged as a WAR.
- **Backend:** Java 17, Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Hibernate, MySQL 8.
- **Frontend / Views:** JSP (`src/main/webapp/WEB-INF/views`), HTML, CSS, JavaScript (`src/main/resources/static`).
- **Tools:** Maven Wrapper (`.\mvnw.cmd`), Postman CLI for API engineering tasks.

---

## 2. Build and Configuration
- Use the checked-in Maven wrapper (`.\mvnw.cmd`) for all Maven commands.
- Run the application using the wrapper's Spring Boot run goal when needed.
- **Database Credentials:** Database connection settings come strictly from environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`). Do not commit credentials or hard-code secrets.
- Keep generated build outputs (such as the `target/` directory) out of version control and source changes.

---

## 3. Source Directory Layout
- `src/main/java/.../controller`: REST (`/api/v1/`) and web (`/web/`) controllers.
- `src/main/java/.../service` & `service/impl`: Service contracts and business workflows (`AcademicManagementServiceImpl`, etc.).
- `src/main/java/.../repository`: Spring Data repositories and academic repository registry.
- `src/main/java/.../entity`: JPA persistence entities (`EntityChangeLog`, `MenuTemplate`, etc.).
- `src/main/java/.../dto`: API and view data transfer objects (`AcademicRecordDTO`, etc.).
- `src/main/java/.../security` & `config`: Authentication, authorization configuration, and auditing setup.
- `src/main/webapp/WEB-INF/views`: JSP pages and fragments.
- `src/main/resources/static`: CSS, JavaScript, and asset files.
- `src/test`: Automated tests.

---

## 4. Domain & Architecture Guidance
- **Tenant Isolation:** Preserve strict tenant isolation for every read and write. Resolve the authenticated user's tenant on the server and validate that referenced records belong to that same tenant. **Never** rely on a client-supplied `tenantId` parameter for authorization.
- **Reusability & Patterns:** Reuse existing entity, DTO, repository, service, controller, and JSP conventions. Academic modules share `AcademicRecordDTO`, `AcademicManagementServiceImpl`, and abstract base controllers (`AbstractAcademicRestController`, `AbstractAcademicWebController`). Inspect these before writing new academic features.
- **Route Conventions:** Keep REST endpoints under `/api/v1/` and server-rendered JSP pages under `/web/`, following neighboring module route and response conventions.
- **DTOs & Validation:** Use request DTOs and bean validation for inputs. Keep persistence entities out of API request/response contracts unless an established endpoint already does so.
- **Entity Integrity:** Preserve existing status, soft-delete, duplicate-check, relationship-validation, and entity change-log behaviors when modifying records.
- **Security & Permissions:** Menu templates organize menus by user type, but menu visibility alone does not enforce endpoint authorization. Apply and verify explicit server-side access checks for protected operations.
- **Data Formats:** Display date-time values consistently as `yyyy-MM-dd HH:mm` and date-only values as `yyyy-MM-dd`. Keep stored timestamps and machine-readable ISO values unchanged; use lowercase `yyyy` for calendar-year formatting.
- **Avoid Unrelated Refactors:** Keep changes scoped to the task at hand and preserve existing user changes in the working tree.

---

## 5. Current Implementation Status & Roadmap

### Phase 1: Core System & Academic Foundation (Complete — implementation complete)
* **Implemented:** Tenant Management, User Management, Menu Management, Menu Template Management, User Template Management, Country/State/City Management, Academic Foundation (Faculty, Department, Program, Academic Year, Semester, Section, Subject, Program Subject, Student Management, Student Enrollment), Role/Permission enforcement, and Audit Log search management.
* **Role and Permission Management:** `RolePermissionPolicy` maps authenticated user types to explicit Spring Security permission authorities. `SecurityConfig` applies permission checks to system administration and academic routes, while academic write handlers enforce write permission at method level.
* **Audit Log Management:** System administrators can search paginated `EntityChangeLog` records through `/api/v1/audit-logs` and `/web/audit-logs`, filtering by text, entity, entity ID, action, actor, and date range.

### Phase 2: Essential College Operations (In progress)
1. Student Admission Management (Complete)
2. Teacher and Staff Management (Complete)
3. Student Attendance
4. Timetable Management
5. Examination Management
6. Marks Entry and Verification
7. Result Processing and GPA Calculation
8. Student Certificate Management
9. Fee Structure Management
10. Student Fee Collection
11. Receipt Generation
12. Notice and Notification Management
13. Student Portal & Teacher Portal

* **Student Admission Management:** Tenant-scoped application records and review transitions; accepting an application atomically creates the existing student and student-enrollment records. Applicant and decision changes are audited.
* **Teacher and Staff Management:** Tenant-scoped employee profiles for teachers and staff, with department validation, status handling, soft deletion, and audit history. This manages employee profiles; login account provisioning remains part of User Management.
* **Verification status:** `mvnw.cmd -q -DskipTests compile` completed successfully after these Phase 2 changes. API runtime behavior and authenticated Postman requests have not been exercised.

### Phase 3: Advanced Academic & Administrative Features (Pending)
1. Library Management
2. Scholarship and Discount Management
3. Online Payment Integration & Financial Reconciliation
4. Staff Leave and Payroll Management
5. Classroom and Laboratory Management
6. Parent Portal, Assignment & Learning Management
7. Complaint, Feedback Management, Reports, and Analytics

### Phase 4: Optional Enterprise Features (Pending)
* Hostel, Transportation, Inventory/Asset, Recruitment, Alumni Management, Advanced Dashboards, Tenant Subscription Billing, External University Integration, Biometric Attendance, and Advanced Security Monitoring.

---

## 6. Verification Protocol
- **Code Changes:** Prefer the narrowest relevant Maven test or compile command first, followed by broader verification when appropriate.
- **Claiming Success:** Do not claim a change is verified unless the corresponding command was run successfully. Report skipped verification clearly.
- **API Testing:** For API contract, collection, or endpoint work, use the installed Postman CLI where applicable; do not publish or modify shared Postman resources unless explicitly requested.
