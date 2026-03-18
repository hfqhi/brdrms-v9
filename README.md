# Barangay Document Request and Management System

A console-based Java application that digitizes the document request process for barangay offices. Residents register, submit document requests, and track their status. Administrators manage the full request lifecycle, resident accounts, document types, and maintain a complete audit trail.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (JDK 8+, NetBeans 8.2) |
| Database | MySQL 5.1.23 via XAMPP |
| JDBC Driver | mysql-connector-java 5.1.49 |
| Password Hashing | jBCrypt 0.4 |
| Interface | Console / Terminal |

---

## Features

### Resident
- Register and await admin approval before logging in
- Submit document requests from an admin-managed catalogue with fees
- Track request status — Pending, Approved, Rejected
- Edit or cancel pending requests
- View who processed each request and when
- Update profile (name, contact, address, password)

### Admin
- Approve or reject resident registrations
- Process document requests (approve / reject) with optional remarks
- Archive and restore requests — hard delete from the archive only
- Manage document types: add, edit, archive, restore, remove
- View, search, and archive resident accounts — restore or permanently delete from the archive
- Full audit history log with keyword search and filter by action type
- Change admin password from within the system

---

## Database Schema — `db_brdrms_v1`

Six tables with enforced foreign key relationships.

```
tbl_admin           Barangay staff accounts
tbl_residents       Approved resident accounts
tbl_document_type   Admin-managed document list with fees
tbl_document_request Full request lifecycle (PENDING → APPROVED/REJECTED)
tbl_registration    Pending sign-up queue
tbl_history         Append-only admin audit trail
```

**Key constraints:**

| FK | Behavior | Reason |
|---|---|---|
| `resident_id` | CASCADE on delete | Resident deleted → all their requests deleted |
| `doc_type_id` | RESTRICT on delete | Cannot delete a type with active requests |
| `admin_id` | SET NULL on delete | Admin deleted → records kept, link cleared |

---

## Project Structure

```
BarangaySystemV9/
├── src/com/barangay/system/
│   ├── database/
│   │   └── DbConnection.java          Singleton JDBC connection
│   ├── model/
│   │   ├── Admin.java
│   │   ├── Resident.java
│   │   ├── DocType.java
│   │   ├── DocRequest.java
│   │   ├── Registration.java
│   │   └── History.java
│   ├── repo/
│   │   ├── BaseRepo.java              Shared resetAutoIncrement()
│   │   ├── AdminRepo.java
│   │   ├── ResidentRepo.java
│   │   ├── DocTypeRepo.java
│   │   ├── DocRequestRepo.java
│   │   ├── RegistrationRepo.java
│   │   └── HistoryRepo.java
│   ├── service/
│   │   ├── BaseService.java           Shared safeList(SqlSupplier)
│   │   ├── AdminService.java
│   │   ├── ResidentService.java
│   │   ├── DocTypeService.java
│   │   ├── DocRequestService.java
│   │   ├── RegistrationService.java
│   │   └── HistoryService.java
│   ├── ui/
│   │   ├── Console.java               All display + input utilities
│   │   ├── AuthMenu.java
│   │   ├── ResidentMenu.java
│   │   ├── AdminMenu.java
│   │   └── admin/
│   │       ├── RequestsMenu.java
│   │       ├── DocTypeMenu.java
│   │       ├── ResidentsMenu.java
│   │       ├── RegistrationsMenu.java
│   │       └── HistoryMenu.java
│   ├── util/
│   │   ├── StringUtil.java
│   │   └── Setup.java                 One-time seed utility
│   └── main/
│       └── App.java                   Entry point
└── lib/
    ├── mysql-connector-java-5.1.49-bin.jar
    └── jbcrypt-0.4.jar
```

---

## Architecture

The system follows a strict four-layer architecture. Calls flow downward only — the UI never writes SQL, and the repository never contains business logic.

```
┌─────────────────────────────────────────────────────┐
│  UI Layer                                           │
│  Console · AuthMenu · ResidentMenu · AdminMenu      │
│  admin sub-menus: Requests · DocTypes · Residents   │
│  Registrations · History                            │
└──────────────────────┬──────────────────────────────┘
                       │ calls service methods
┌──────────────────────▼──────────────────────────────┐
│  Service Layer                                      │
│  Business logic · BCrypt auth · status guard        │
│  All SQLException caught here → returns String      │
└──────────────────────┬──────────────────────────────┘
                       │ calls repo methods
┌──────────────────────▼──────────────────────────────┐
│  Repository Layer                                   │
│  SQL via PreparedStatement · mapRow() constructors  │
│  buildList() helper · throws SQLException up        │
└──────────────────────┬──────────────────────────────┘
                       │ gets connection
┌──────────────────────▼──────────────────────────────┐
│  Database Layer                                     │
│  DbConnection singleton · db_brdrms_v1              │
└─────────────────────────────────────────────────────┘

  Model POJOs (Admin · Resident · DocType · DocRequest
  Registration · History) travel between all layers.
```

---

## Password Security

All passwords are hashed with **BCrypt** (jBCrypt 0.4, cost factor 12). No plain-text password is ever stored anywhere in the database.

- **Registration:** `BCrypt.hashpw()` is called in `RegistrationService.submit()` before saving. The hash is copied verbatim to `tbl_residents` on approval — never re-hashed.
- **Login:** `BCrypt.checkpw(inputPassword, storedHash)` is called in `AdminService.login()` and `ResidentService.login()`.
- **Password change:** Current password is verified with `checkpw()` before the new password is hashed and saved.
- **Setup.java:** Generates the BCrypt hash for the default admin at runtime — no plain-text password in SQL.

---

## Setup

### Prerequisites
- XAMPP (MySQL 5.1.23)
- NetBeans 8.2 with JDK 8+
- `mysql-connector-java-5.1.49-bin.jar`
- `jbcrypt-0.4.jar`

### Steps

**1. Start XAMPP and enable MySQL.**

**2. Run the SQL schema.**

Open phpMyAdmin → SQL tab → paste and run the full schema from `BarangaySystemV9.md`.

**3. Create the NetBeans project.**

```
New Project → Java Application
Name  : BarangaySystemV9
Class : com.barangay.system.main.App
```

**4. Create all package folders** as shown in the project structure above.
Note: `ui/admin/` is a sub-package of `ui` — create it as a nested package.

**5. Add both JARs to Libraries.**

```
Right-click project → Properties → Libraries → Add JAR/Folder
```

**6. Run Setup.java once.**

```
Right-click Setup.java → Run File
```

This creates the BCrypt-hashed admin account and seeds five default document types.

**7. Run the application.**

Press `F6` or right-click `App.java → Run File`.

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |

> Change the admin password after first login via **[ 6 ] My Account** on the admin dashboard.

---

## Approving New Residents

New residents sign up via **[ 2 ] Register** on the auth screen. They cannot log in until an admin approves their account.

To approve:
```
Admin login → [ 4 ] Registrations → [ 1 ] View Pending → [ 2 ] Approve
```

---

## Document Request Lifecycle

```
submit()
   │
   ▼
PENDING ──── process(APPROVED) ──► APPROVED ─┐
   │                                          │
   ├─── process(REJECTED) ───► REJECTED ──────┤
   │                                          │ archive()
   └── cancel() ──────────────────────────────┤
                                              ▼
                                          ARCHIVED ──── restore() ──► (back to active)
                                              │
                                              └── remove() ──► DELETED (irreversible)
```

Only `PENDING` requests can be processed. The status guard in `DocRequestService.process()` blocks re-processing of decided requests. Hard delete is only accessible from within the archive view.

---

## Menu Structure

```
Auth screen
  [ 1 ]  Log In
  [ 2 ]  Register
  [ 0 ]  Exit

Resident Dashboard
  [ 1 ]  Requests  →  View · Find · New · Edit · Cancel · [0] Back
  [ 2 ]  Profile   →  View · Edit · [0] Back
  [ 0 ]  Log Out

Admin Dashboard
  [ 1 ]  Requests       →  View · Find · Process · Archive · View Archive · [0] Back
  [ 2 ]  Document Types →  New · Edit · Archive · Restore · Remove · [0] Back
  [ 3 ]  Residents      →  View · Find · Archive · Archived Residents · [0] Back
  [ 4 ]  Registrations  →  View Pending · Approve · Reject · [0] Back
  [ 5 ]  History Log    →  View All · Find · Filter by Type · [0] Back
  [ 6 ]  My Account     (admin password change)
  [ 0 ]  Log Out
```

---

## Default Document Types

Seeded by `Setup.java` on first run.

| Document | Fee |
|---|---|
| Barangay Clearance | PHP 50.00 |
| Certificate of Residency | PHP 50.00 |
| Certificate of Indigency | FREE |
| Business Permit Endorsement | PHP 100.00 |
| Certificate of Good Moral Character | PHP 50.00 |

Admins can add, edit, archive, restore, or remove document types at any time via **[ 2 ] Document Types**.

---

## Version History

| Version | Key Changes |
|---|---|
| V9 | Split AdminMenu into 5 sub-classes · BaseRepo/BaseService/StringUtil extracted · admin password change · resident archive/restore/delete · history search + filter · centered menu layout · y/n confirmation · chronological history ordering · updated_at columns · indexes added |
| V8 | BCrypt hashing · 6-table schema with tbl_registration and tbl_history · sub-menu structure · Setup.java seed utility |
| V7 | Unified tbl_users (role-based) · dynamic tbl_document_types with fees · tbl_payments · admin tracking on requests |
| V6 | Centered UI · tbl_ prefix · registration approval system |

---

## License

For academic and educational use.
