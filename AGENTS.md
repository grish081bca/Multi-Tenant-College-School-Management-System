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
- **Platform Admin Access :** `SUPER_ADMIN` has read and write access across project modules and may select a tenant where a module's records are tenant-owned. `SYSTEM_ADMIN` has read-only access to academic, admissions, and employee modules; never grant it write permissions for those modules. Enforce this in the role permission policy and at every REST and web write handler; hiding write links in menus is only a usability measure.
- **Future Module Access :** Every new module must explicitly define `SUPER_ADMIN` and `SYSTEM_ADMIN` access in the permission policy, route security, method-level write checks, and menu template. For tenant-owned data, platform-admin tenant selection is a scope selector only: validate the selected tenant server-side, require it for writes and single-record operations, allow an unfiltered platform-admin list to span tenants when intended, and always derive tenant-account scope from the authenticated principal. Give `SYSTEM_ADMIN` read routes only for academic, admissions, and employee modules.
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
3. Student Attendance (Complete)
4. Timetable Management (Complete)
5. Examination Management (Complete)
6. Marks Entry and Verification (Complete)
7. Result Processing and GPA Calculation (Complete)
8. Student Certificate Management
9. Fee Structure Management
10. Student Fee Collection
11. Receipt Generation
12. Notice and Notification Management
13. Student Portal & Teacher Portal

* **Student Admission Management:** Tenant-scoped application records and review transitions; accepting an application atomically creates the existing student and student-enrollment records. Applicant and decision changes are audited.
* **Teacher and Staff Management:** Tenant-scoped employee profiles for teachers and staff, with department validation, status handling, soft deletion, and audit history. This manages employee profiles; login account provisioning remains part of User Management.
* **Student Attendance:** Tenant-scoped daily attendance for active student enrollments, with present, absent, late, and excused statuses, duplicate-per-student-per-day checks, and audited changes.
* **Timetable Management:** Tenant-scoped weekly class slots linked to sections and program subjects, with optional active teacher assignment and overlap checks for sections, teachers, and rooms.
* **Examination Management:** Tenant-scoped examination schedules linked to sections and program subjects, with validated times, maximum marks, and section schedule overlap checks. Marks entry remains a separate module.
* **Marks Entry:** Tenant-scoped marks are recorded once per examination and student enrollment. The service validates that the examination, enrollment, section, program, semester, subject, and student belong to the same tenant, and that the enrollment matches the examination section and program subject. Marks must be non-negative and cannot exceed the examination maximum.
* **Marks Verification:** Marks move from `PENDING` to `VERIFIED` or `REJECTED`. Rejection requires a reason; verified marks cannot be edited. Updating rejected marks resets them to pending for review. Entry and verification transitions are audited.
* **Result Processing and GPA:** Results can be processed for an active enrollment only after every active examination in its section and semester has verified marks. Results retain totals and a per-subject breakdown. The current GPA rule converts each subject percentage linearly to a 4.0 scale (`percentage / 25`) and weights the subject grade points by credit hours; all included subjects must have positive credit hours. This is an interim rule until the institution supplies a grading-band policy.
* **Role access:** `SUPER_ADMIN` has read/write/verify/process access with server-validated tenant selection. `SYSTEM_ADMIN` has read-only access to marks and results. College administrators, principals, and branch administrators can enter and verify marks and process results. Teachers have read-only access. Route security, method checks, role permissions, and menu templates are configured for these modules.
* **Verification status:** `mvnw.cmd -q -DskipTests compile` completed successfully after Marks Entry, Marks Verification, and Result Processing/GPA implementation. API runtime behavior, authenticated requests, and Postman runs have not been exercised.

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
