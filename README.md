# 🏛️ Barangay Document Request System

A console-based Java application that digitizes the document request process of a barangay office. Residents can register, log in, and submit document requests online, while administrators can review, approve, or reject each request through a structured admin panel.

Built with **Java (NetBeans 8.2)**, **JDBC**, and **MySQL 5.1.23 via XAMPP**.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [System Flow](#system-flow)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Default Credentials](#default-credentials)
- [Input Formatting Rules](#input-formatting-rules)

---

## Features

### Resident (User)
- **Sign Up** — Create an account with username uniqueness validation
- **Log In** — Authenticate with username and password
- **Submit Request** — Choose from 5 document types and provide a purpose
- **View My Requests** — Requests are grouped into Pending, Approved, and Rejected sections
- **Edit Request** — Modify the purpose of any Pending request
- **Cancel Request** — Delete a Pending request (reuses the freed ID)
- **View / Update Profile** — Update full name, address, and password

### Administrator
- **Dashboard** — Shows a live count of Pending requests on login
- **View All Requests** — Grouped into Pending / Approved / Rejected with per-section counts
- **Approve / Reject** — Lists all Pending requests with purpose visible before asking for an ID; shows the selected detail again before confirming
- **View Request Details** — Browse a compact list then select any ID for full detail
- **Delete Request** — Shows the record in full before asking for confirmation
- **View All Residents** — Full table of registered accounts with join date

### System-wide
- AUTO_INCREMENT reset after every delete — freed IDs are reused by the next insert
- Input auto-formatting — Full Name and Address are stored in Title Case; Purpose and Admin Remarks are stored in Sentence Case
- 80-character wide separators for clean full-screen terminal display
- Layered architecture — UI, Service, Repository, and Database layers are fully separated

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java SE 6+ |
| IDE | NetBeans 8.2 |
| Database | MySQL 5.1.23 (XAMPP) |
| DB Driver | mysql-connector-java-5.1.49 |
| DB Access | JDBC with PreparedStatement + try-with-resources |

---

## Project Structure

```
BarangaySystem/
├── src/
│   └── com/
│       └── barangay/
│           └── system/
│               ├── database/
│               │   └── DatabaseConnection.java     # Singleton JDBC connection
│               ├── model/
│               │   ├── User.java                   # User POJO
│               │   └── DocumentRequest.java        # DocumentRequest POJO
│               ├── repository/
│               │   ├── UserRepository.java         # SQL CRUD for users
│               │   └── DocumentRequestRepository.java  # SQL CRUD for requests
│               ├── service/
│               │   ├── UserService.java            # Business logic for users
│               │   └── DocumentRequestService.java # Business logic for requests
│               ├── ui/
│               │   ├── ConsoleHelper.java          # Shared I/O + formatting utilities
│               │   ├── AuthMenu.java               # Login and Sign Up screens
│               │   ├── UserMenu.java               # Resident dashboard
│               │   └── AdminMenu.java              # Admin control panel
│               └── main/
│                   └── Main.java                   # Entry point + dependency wiring
└── lib/
    └── mysql-connector-java-5.1.49-bin.jar
```

---

## Database Schema

Two tables with a foreign key relationship:

```
users
─────────────────────────────────────────
id          INT  PK  AUTO_INCREMENT
username    VARCHAR(50)  UNIQUE NOT NULL
password    VARCHAR(255) NOT NULL
full_name   VARCHAR(100) NOT NULL
address     VARCHAR(255) NOT NULL
role        ENUM('USER', 'ADMIN')
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP


document_requests
─────────────────────────────────────────
id              INT  PK  AUTO_INCREMENT
user_id         INT  FK -> users(id)  ON DELETE CASCADE
document_type   VARCHAR(100) NOT NULL
purpose         VARCHAR(255) NOT NULL
status          ENUM('PENDING', 'APPROVED', 'REJECTED')
admin_remarks   VARCHAR(255) NULL
request_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
processed_date  TIMESTAMP NULL
```

> `processed_date` is kept nullable because MySQL 5.1 allows only one `TIMESTAMP` column with `DEFAULT CURRENT_TIMESTAMP` per table. Setting it to `NULL` avoids MySQL error **#1293**.

> Deleting a user cascades and removes all their document requests automatically.

---

## System Flow

```
START
  │
  ▼
Verify DB connection
  │  Failed ──► Print error and exit
  │
  ▼
┌─────────────────────────────┐
│  AUTH MENU                  │
│  [1] Log In                 │
│  [2] Sign Up                │
│  [3] Exit                   │
└─────────────────────────────┘
  │
  ├── Sign Up
  │     Enter username, password, full name, address
  │     ├── Username uniqueness check
  │     ├── Password length validation (min 6 chars)
  │     └── Save to DB ──► return to Auth Menu
  │
  ├── Log In
  │     Match username + password in DB
  │     ├── Failed ──► show error, return to Auth Menu
  │     └── Success ──► check role
  │                       │
  │            ┌──────────┴──────────┐
  │            │                     │
  │          ADMIN                  USER
  │            │                     │
  │            ▼                     ▼
  │   ┌─────────────────┐  ┌──────────────────────┐
  │   │  ADMIN PANEL    │  │  RESIDENT DASHBOARD   │
  │   │                 │  │                       │
  │   │ View All        │  │ View My Requests      │
  │   │   PENDING ──────┤  │   PENDING section     │
  │   │   APPROVED      │  │   APPROVED section    │
  │   │   REJECTED      │  │   REJECTED section    │
  │   │                 │  │                       │
  │   │ Approve/Reject  │  │ Submit New Request    │
  │   │   List PENDING  │  │   Pick document type  │
  │   │   Enter ID      │  │   Enter purpose       │
  │   │   Confirm       │  │                       │
  │   │   Add Remarks   │  │ Edit PENDING Request  │
  │   │                 │  │   List PENDING        │
  │   │ View Details    │  │   Enter ID            │
  │   │   List all      │  │   Enter new purpose   │
  │   │   Enter ID      │  │                       │
  │   │   View detail   │  │ Cancel PENDING Request│
  │   │                 │  │   List PENDING        │
  │   │ Delete Request  │  │   Enter ID            │
  │   │   List all      │  │   Confirm             │
  │   │   Enter ID      │  │                       │
  │   │   View detail   │  │ View / Update Profile │
  │   │   Confirm       │  └──────────────────────┘
  │   │                 │
  │   │ View Residents  │
  │   └─────────────────┘
  │            │
  └────────────┴── Logout ──► back to Auth Menu
  │
  ▼
EXIT
```

---

## Architecture

The system follows a strict **4-layer architecture**. Each layer only communicates with the layer directly below it — the UI never writes SQL, and the repository never reads from the console.

```
┌──────────────────────────────────────────────────────┐
│  UI Layer                                            │
│  AuthMenu  UserMenu  AdminMenu  ConsoleHelper        │
│  Reads input, displays output, calls Service layer   │
└───────────────────────┬──────────────────────────────┘
                        │ calls
┌───────────────────────▼──────────────────────────────┐
│  Service Layer                                       │
│  UserService  DocumentRequestService                 │
│  Validates input, enforces business rules,           │
│  returns String result messages to the UI            │
└───────────────────────┬──────────────────────────────┘
                        │ calls
┌───────────────────────▼──────────────────────────────┐
│  Repository Layer                                    │
│  UserRepository  DocumentRequestRepository           │
│  Executes SQL using PreparedStatement + try-with-    │
│  resources. Resets AUTO_INCREMENT after every DELETE │
└───────────────────────┬──────────────────────────────┘
                        │ uses
┌───────────────────────▼──────────────────────────────┐
│  Database Layer                                      │
│  DatabaseConnection (Singleton)                      │
│  Holds one shared JDBC Connection for the JVM        │
│  session. Reconnects automatically if closed.        │
└──────────────────────────────────────────────────────┘
```

---

## Getting Started

### Prerequisites
- [XAMPP](https://www.apachefriends.org/) with MySQL service running
- [NetBeans IDE 8.2](https://netbeans.apache.org/)
- JDK 6 or higher
- [mysql-connector-java-5.1.49](https://dev.mysql.com/downloads/connector/j/5.1.html)

### Setup

**1. Start XAMPP**
Open the XAMPP Control Panel and start the **MySQL** service.

**2. Create the database**
Open **phpMyAdmin** → click the **SQL** tab → paste and run the full schema from `schema.sql`.

**3. Create the NetBeans project**
```
File → New Project → Java → Java Application
Project Name : BarangaySystem
Main Class   : com.barangay.system.main.Main
```

**4. Add the MySQL driver**
```
Right-click project → Properties → Libraries → Add JAR/Folder
Select: mysql-connector-java-5.1.49-bin.jar
```

**5. Create the package folders under `src/`**
Match the structure shown in the [Project Structure](#project-structure) section above.

**6. Copy the source files**
Paste each `.java` file into its corresponding package folder.

**7. Run**
Press `F6` or click the green Run button.

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |

> The admin account is inserted automatically by the SQL schema. You can change the password after your first login via the profile update screen (feature can be added) or directly in phpMyAdmin.

---

## Input Formatting Rules

All text inputs are automatically formatted before being saved to the database. Users do not need to type in any specific case.

| Field | Format Applied | Example Input | Stored As |
|---|---|---|---|
| Full Name | Title Case | `juan dela cruz` | `Juan Dela Cruz` |
| Address | Title Case | `block 4 lot 12 mabini st` | `Block 4 Lot 12 Mabini St` |
| Purpose | Sentence Case | `for employment purposes` | `For employment purposes` |
| Admin Remarks | Sentence Case | `incomplete requirements` | `Incomplete requirements` |
| Username | As typed (no formatting) | `jdelacruz` | `jdelacruz` |
| Password | As typed (no formatting) | `pass123` | `pass123` |

---

## Available Document Types

1. Barangay Clearance
2. Certificate of Residency
3. Certificate of Indigency
4. Business Permit Endorsement
5. Certificate of Good Moral Character

---

*Built for academic and local government digitization purposes.*
