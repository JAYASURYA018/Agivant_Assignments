# ELM — Employee Leave Management System

A web application designed to streamline the request, approval, and tracking of employee leaves. This project is built using a **Vanilla HTML/CSS/JS frontend** and a **Spring Boot REST API backend** connected to a **MySQL database**.

---

## 🏗️ System Architecture & Workflow

ELM is structured into three main layers:

```
┌─────────────────┐         REST API (JSON)         ┌─────────────────┐
│    FRONTEND     │ ◄─────────────────────────────► │    BACKEND      │
│  (Port 3000)    │                                 │  (Port 8081)    │
│                 │                                 │                 │
│ HTML/CSS/JS     │                                 │ Spring Boot REST│
│ served locally  │                                 │ Hibernate / JPA │
└─────────────────┘                                 └────────┬────────┘
                                                             │
                                                             │ SQL
                                                             ▼
                                                    ┌─────────────────┐
                                                    │    DATABASE     │
                                                    │  MySQL (3306)   │
                                                    └─────────────────┘
```

### Key Workflows:
1. **Authentication**: Users log in on the frontend. A successful credentials match returns an employee profile JSON from the server, which is stored in the browser's `localStorage` to manage active sessions.
2. **Leave Request**: An employee selects a date range. The backend dynamically calculates the net working days (excluding weekends and public holidays queried from the database). If the dates do not overlap with any existing requests, a new `PENDING` request is registered.
3. **Manager Approval**: Managers view active requests and trigger approval or rejection endpoints. The database updates the request state instantly, which updates the employee's dashboard counters on reload.

---

## 👥 Features by Role

### Employees
* View leave balances (Casual, Sick, and Earned leaves) and request histories.
* Submit new leave requests with a reason.
* Cancel pending or approved leave requests.
* View a weekly calendar schedule showing which team members are out of office.

### Managers
* Full directory access to create, edit, or delete employee profiles.
* Approve or reject pending leave requests.
* View dynamic admin reports (Total leaves taken per employee, staff with zero leaves, and leave type popularity).

---

## 📸 Application Screenshots

Here is the visual walkthrough of the application in sequence:

### 1. User Authentication & Profile
| Login Screen | User Dashboard |
|---|---|
| ![LogIn page](Images/LogIn%20page.png) *The login page where users enter their email and password to log in.* | ![Dashboard](Images/Dashboard.png) *The home page showing leave balances like Casual, Sick, and Earned leaves.* |

| Edit Profile | Public Holidays List |
|---|---|
| ![Edit Profile User](Images/Edit%20Profile%20User.png) *The profile page where employees can update their personal details.* | ![Holiday](Images/Holiday.png) *The holidays page displaying the list of public holidays.* |

### 2. Leave Application & History
| Apply for Leave | Leave Apply Form |
|---|---|
 ![Leave Apply form](Images/Leave%20Apply%20form.png) *The pop-up form where employees fill in leave dates and details.* |
| ![Leave Request User](Images/Leave%20Request%20User.png) *The page where employees start applying for leave.* |

| Leave History List |
|---|
| ![User View to Leave request](Images/User%20View%20to%20Leave%20request.png) *The page showing all leave requests submitted by the user and their current status.* |

### 3. Team Calendar & Schedule
| Weekly Absences Grid |
|---|
| ![OnLeave Employees](Images/OnLeave%20Employees.png) *The calendar page showing which employees are on leave each day of the week.* |

### 4. Administrative & Manager Panel
| Add Employee Dialog | Pending Requests Grid |
|---|---|
| ![Add Emp by Admin](Images/Add%20Emp%20by%20Admin.png) *The form used by managers to add new employee accounts.* | ![All Leave request to Admin](Images/All%20Leave%20request%20to%20Admin.png) *The admin page where managers see all pending leave requests.* |

| Approval Actions | Recalculated Balances |
|---|---|
| ![Al Leave request Action by Admin](Images/Al%20Leave%20request%20Action%20by%20Admin.png) *The admin page showing the updated requests list after a manager approves or rejects requests.* | ![Dashboard after Leave Accepted](Images/Dashboard%20after%20Leave%20Accepted.png) *The employee's dashboard showing updated leave counts after the manager approved the request.* |

### 5. Backend Database & API Testing
| MySQL Users Table | MySQL Leave Requests Table |
|---|---|
| ![Mysql user data ](Images/Mysql%20user%20data%20.png) *The `employee` table inside MySQL showing registered accounts.* | ![Mysql Leaves data](Images/Mysql%20Leaves%20data.png) *The `leave_request` table inside MySQL showing requested leaves.* |

| Postman Endpoints Suite |
|---|
| ![PostMan Api](Images/PostMan%20Api.png) *Testing and verifying the backend REST APIs inside Postman.* |

---

## 🗄️ Database Schema

The database consists of 3 relational tables:

```
                  ┌──────────────────┐
                  │    EMPLOYEE      │
                  ├──────────────────┤
                  │ id (PK)          │◄────────┐
                  │ employee_id      │         │
                  │ first_name       │         │
                  │ last_name        │         │
                  │ email (UQ)       │         │
                  │ password         │         │
                  │ department       │         │
                  │ role             │         │
                  └──────────────────┘         │
                                               │ Foreign Key
                  ┌──────────────────┐         │ (employee_id)
                  │  LEAVE_REQUEST   │         │
                  ├──────────────────┤         │
                  │ id (PK)          │         │
                  │ employee_id (FK) ├─────────┘
                  │ leave_type       │
                  │ start_date       │
                  │ end_date         │
                  │ number_of_days   │
                  │ status           │
                  │ reason           │
                  └──────────────────┘

                  ┌──────────────────┐
                  │     HOLIDAY      │
                  ├──────────────────┤
                  │ id (PK)          │
                  │ name             │
                  │ holiday_date (UQ)│
                  │ description      │
                  └──────────────────┘
```

* **Note**: We separate `id` (database auto-increment join key) from `employee_id` (the public staff code, e.g. `E101`). This ensures business staff codes can be modified in the future without breaking historical relational records.

---

## 📁 Folder Structure

```
LM/
├── backend/                  ← Spring Boot application folder
│   ├── src/main/java/        ← Java source files (Controllers, Services, Entities)
│   ├── src/main/resources/   ← Config properties, schema.sql, data.sql
│   └── pom.xml               ← Maven dependencies
├── database/                 ← Seed SQL files
└── frontend/                 ← User Interface static files
    ├── index.html            ← Main dashboard
    ├── pages/                ← Sub-pages (login, employees, holidays, etc.)
    ├── js/                   ← JS controllers (api.js, common.js, etc.)
    └── css/                  ← CSS style sheets
```

---

## 🚀 Getting Started

### 1. Setup the Database
Ensure you have a local MySQL server running on port `3306` and create a database named `leave_management`.
On startup, Spring Boot will automatically run the schema and seed data found in the `backend/src/main/resources/data.sql` and `schema.sql` files.

### 2. Start the Spring Boot Backend
Configure your MySQL credentials in `backend/src/main/resources/application.properties` and run:
```bash
cd backend
.\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
* API Server URL: `http://localhost:8081`
* Interactive API Documentation: `http://localhost:8081/swagger-ui.html`

### 3. Start the Frontend
Run a local static server to open the files correctly in your browser:
```bash
cd frontend
python -m http.server 3000
```
Open `http://localhost:3000` in your web browser.

### 4. Default Credentials (Test Accounts)
| Role | Email | Password |
|---|---|---|
| **Manager** | `anupriya.mohan@zylker.com` | `luffy` |
| **Employee** | `liam.john@zylker.com` | `password123` |

---

## 🧪 Running Tests

To run the JUnit unit tests validating the service logic, run:
```bash
cd backend
.\apache-maven-3.9.6\bin\mvn.cmd test
```
All repository queries are mocked using Mockito, ensuring tests run instantly without database dependencies.
