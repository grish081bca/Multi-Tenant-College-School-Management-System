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
- **Avoid Unrelated Refactors:** Keep changes scoped to the task at hand and preserve existing user changes in the working tree.

---

## 5. Current Implementation Status & Roadmap

### Phase 1: Core System & Academic Foundation (In Progress — ~80% Complete)
* **Implemented:** Tenant Management, User Management, Menu Management, Menu Template Management, User Template Management, Country/State/City Management, Academic Foundation (Faculty, Department, Program, Academic Year, Semester, Section, Subject, Program Subject, Student Management, Student Enrollment).
* **Incomplete / Pending Tasks to Close Phase 1:**
    1. **Role and Permission Management:** Integrate role-based security checks into `SecurityConfig` and controllers, expanding beyond basic `UserRole` enums.
    2. **Audit Log Management:** Implement a dedicated system-wide administrator search UI and API endpoint for `EntityChangeLog`.

### Phase 2: Essential College Operations (Pending)
1. Student Admission Management
2. Teacher and Staff Management
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