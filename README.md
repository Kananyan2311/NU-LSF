# 🐾 NU-LSF: National University Lost &amp; Found System

<div align="center">

![NU-LSF System Banner](docs/images/nu-lsf-banner.svg)

[![Java 25](https://img.shields.io/badge/Java-25-orange.svg?style=flat-square&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Enterprise%20MVC-6DB33F.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Pure%20Java%20Templates-005F0F.svg?style=flat-square&logo=thymeleaf)](https://www.thymeleaf.org/)
[![Theme](https://img.shields.io/badge/Theme-NU%20Blue%20%26%20Gold-35408e.svg?style=flat-square)](#-national-university-identity--swiss-minimalist-ui)
[![Design](https://img.shields.io/badge/Style-Swiss%20Minimalist-000000.svg?style=flat-square)](#-national-university-identity--swiss-minimalist-ui)

**Official Web Application for National University Philippines**  
*Reconnecting Bulldogs with what matters through a secure, high-integrity campus property management grid.*

</div>

---

## 🌟 Overview

**NU-LSF (National University Lost &amp; Found)** is a web platform written entirely in **Java Spring Boot**. It provides a centralized repository where students, faculty, and security administrators can report missing property, surrender items discovered on campus, communicate via two-way messaging, verify ownership proof, and generate official printable handover passes.

Designed following the **Swiss Minimalist Design System** and themed with the proud **Navy Blue (`#35408e`) and Gold (`#fcd116`)** colors of National University Philippines, the application provides high contrast, zero-radius rectangular controls, an architectural 12-column grid, and a persistent 3-column index rail.

---

## 🚀 10+ Features Overview

| # | Feature | Description |
|---|---|---|
| **01** | **Dual User &amp; Admin Web Signup** | Comprehensive self-service registration for both Students/Faculty and LSF Security Administrators, secured by an authorized **NU Admin Security Clearance Key** (`NU-ADMIN-2026`). |
| **02** | **Two-Way Messaging System** | Direct live messaging between Students and the LSF Office. Students can inquire about specific items or general issues; Admins have an institutional Inbox to reply directly to students. |
| **03** | **Executive Admin Operations Portal** | Dedicated web dashboard with 4 core suites: **Claims Review**, **Physical Intake Check-in**, **Live Inventory Control**, and **Security Audit Logs**. |
| **04** | **Ownership Proof &amp; Verification System** | When a student claims a found item, they provide detailed identifying marks (wallpaper, serials, scratches). Admins inspect the proof and generate a **Unique Verification Pickup Code** (`NU-CXXXXXX`). |
| **05** | **Printable Official Handover Release Pass** | Approved claimants receive an official **Lost Property Release Slip** complete with retrieval barcode representation, claimant details, item custody info, and signature release lines for office sign-off. |
| **06** | **Automated Smart Match Correlation Engine** | An intelligent Java scoring algorithm compares newly reported lost items against surrendered found items using weighted matching (Category +40, Color +25, Campus Location +20, Keywords +15). |
| **07** | **Physical Locker &amp; Bin Custody Workflow** | Campus security check-in workflow allowing administrators to log physical storage bin locations (e.g., `Locker A-04, Cabinet 2, Safe Drawer 1`) before items become available in the catalog. |
| **08** | **Multi-Criteria Advanced Search &amp; Filtering** | Pure server-side search across title, description, and location keywords, combined with category filters (Electronics, IDs & Cards, Keys, Books, etc.) and lifecycle statuses. |
| **09** | **Real-Time In-App Alert &amp; Notification Center** | Centralized notification tray alerting users whenever their claim status changes, when an admin replies to a message, or when a smart match is discovered. |
| **10** | **Enterprise Activity Audit Trail** | Tamper-evident activity logs capturing every status change, claim filing, approval, surrender reception, and message dispatch with exact timestamps and officer identities. |
| **11** | **Instant CSV Inventory &amp; Claims Export** | One-click export for university reporting: download complete property inventory (`items.csv`) and claims ledger (`claims.csv`). |
| **12** | **Student &amp; Staff Profile Management** | Edit profile details, view personal activity statistics (total reports filed, claims filed), and update student/employee ID numbers. |

---

## 🎨 National University Identity &amp; Swiss Minimalist UI

The visual interface blends **National University Philippines branding** with the **Swiss Minimalist design system**:

- **Color Palette**:
  - Ground: `#ffffff` (Crisp White)
  - Ink: `#000000` & `#111111` (Deep Black)
  - National University Blue: `#35408e` (Royal Navy)
  - National University Gold: `#fcd116` (Bulldog Gold)
  - Signals: `#c90000` (Signal Red) & `#0b7816` (Verified Green)
- **Grid Layout**: 
  - Standard **12-column desktop grid**.
  - **3-column Index Rail** on the left displaying structural indices (`INDEX 01 / SECURITY`, `INDEX 02 / REPOSITORY`, etc.), system disclosures, and filter forms.
  - **9-column Content Matrix** on the right for items, forms, and operations.
- **Typography**: 
  - Bound to **Host Grotesk** / Swiss Minimalist font stack.
  - Heavy display headings, tightly tracked, with high editorial clarity.
- **Square Controls (`--dp-radius-control: 0px`)**:
  - Zero rounded corners.
  - Exposed 2px dividing rules (`--dp-border-rule: 2px`).
  - High-contrast button states with visible focus outlines for accessibility.

---

## 💻 Tech Stack &amp; Architecture

```
                       [ National University Web Client ]
                                      │
                         Thymeleaf Server-Rendered HTML
                                      │
                             [ Spring Security ]
                         (Role: USER vs ROLE_ADMIN)
                                      │
                     ┌────────────────┴────────────────┐
                     ▼                                 ▼
             [ Web Controllers ]              [ REST Endpoints ]
          • HomeController                 • ConversationRestController
          • AuthController                 • UserController
          • CatalogController              • ItemController
          • ClaimController
          • MessagingWebController
          • AdminWebController
          • NotificationWebController
          • ProfileWebController
                     │
              [ Java Service Layer ]
          • AuthService                    • ItemService (Smart Match Engine)
          • ClaimService                   • MessagingService
          • FileStorageService             • NotificationService
          • ReportExportService            • AuditService
                     │
              [ JPA Repository Layer ]
          • UserRepository                 • ItemRepository
          • ClaimRepository                • ConversationRepository
          • MessageRepository              • NotificationRepository
          • AuditLogRepository
                     │
         [ MySQL 8.x / Embedded Database ]
```

- **Backend**: Java 25 / Java 21+ with Spring Boot
- **Frontend**: Pure Java Thymeleaf server-side templates (No heavy client-side JavaScript frameworks)
- **Security**: Spring Security 6 with BCrypt password hashing & DaoAuthenticationProvider
- **Persistence**: Spring Data JPA / Hibernate ORM with `ddl-auto=update`

---

## 🔑 Pre-Seeded Accounts for Testing

The system automatically initializes default accounts on first startup so you can immediately explore both user and admin capabilities:

### 🛡️ Administrator Account
- **Email:** `admin@national-u.edu.ph`
- **Password:** `admin123`
- **Role:** `ADMIN`
- **Dashboard:** Redirects automatically to `/admin/dashboard`
- **Capabilities:** Approve/Reject claims, intake found items, update locker bins, manage inventory, view audit logs, reply to all student messages, export CSV reports.

### 🎓 Student / User Account
- **Email:** `student@students.nu-laguna.edu.ph`
- **Password:** `student123`
- **Role:** `USER`
- **Dashboard:** Redirects automatically to `/catalog`
- **Capabilities:** File lost/found reports, file ownership claims, view claim status, print handover passes, message admin, receive alerts.

### 🔐 Registering New Accounts
- Go to `/auth/login`
- Choose **Student / Faculty User** or **LSF Office Administrator**
- If selecting Administrator, enter the security clearance key:
  ```
  NU-ADMIN-2026
  ```

---

## 🛠️ Getting Started &amp; Running

### Prerequisites
- **JDK 21 or JDK 25** installed.
- **MySQL Database** running on `localhost:3306` with database `nulsf` (or your configured database in `application.properties`).

### 1. Configure Database Connection
Inspect `src/main/resources/application.properties`:
```properties
spring.application.name=nulsf
spring.datasource.url=jdbc:mysql://localhost:3306/nulsf
spring.datasource.username=root
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update

# Upload folder for property photographs
app.upload.dir=C:/NULSFimage
server.port=8081
```

### 2. Compile and Run
Open your terminal in the project directory and run:

```powershell
# Using the Maven Wrapper on Windows:
.\mvnw.cmd spring-boot:run
```

Or on Linux / macOS:
```bash
./mvnw spring-boot:run
```

### 3. Open in Browser
Navigate to:
```
http://localhost:8081
```

---

## 📁 Project Structure

```
c:\Users\Kananyan\Downloads\NULSF-main\NULSF-main
├── docs/
│   └── images/
│       └── nu-lsf-banner.svg             # Visual banner
├── src/
│   ├── main/
│   │   ├── java/com/example/nulsf/
│   │   │   ├── config/
│   │   │   │   ├── CustomSuccessHandler.java   # Auto-redirects Admin vs User
│   │   │   │   ├── SecurityConfig.java         # Spring Security routes
│   │   │   │   └── WebMvcConfig.java           # Static upload handlers
│   │   │   ├── controllers/
│   │   │   │   ├── AdminWebController.java     # Admin operations & CSV export
│   │   │   │   ├── AuthController.java         # Dual registration & login
│   │   │   │   ├── CatalogController.java      # Search, filter, item details
│   │   │   │   ├── ClaimController.java        # Claims & printable pass
│   │   │   │   ├── HomeController.java         # Landing page & stats
│   │   │   │   ├── MessagingWebController.java # Two-way messaging
│   │   │   │   ├── NotificationWebController.java
│   │   │   │   ├── ProfileWebController.java
│   │   │   │   └── ConversationRestController.java
│   │   │   ├── model/
│   │   │   │   ├── AuditLog.java               # Security audit trail
│   │   │   │   ├── Claim.java                  # Ownership claim entity
│   │   │   │   ├── Conversation.java           # Message thread
│   │   │   │   ├── Item.java                   # Lost & found item entity
│   │   │   │   ├── Message.java                # Chat messages
│   │   │   │   ├── Notification.java           # Real-time alerts
│   │   │   │   └── User.java                   # User entity with UserDetails
│   │   │   ├── repository/                     # Spring Data JPA repositories
│   │   │   ├── services/
│   │   │   │   ├── AuditService.java
│   │   │   │   ├── AuthService.java            # Passcode verification
│   │   │   │   ├── ClaimService.java           # Pass generator
│   │   │   │   ├── CurrentUserHelper.java
│   │   │   │   ├── DataInitializer.java        # Auto-seeds default users & items
│   │   │   │   ├── FileStorageService.java     # Photo upload handler
│   │   │   │   ├── ItemService.java            # Smart match algorithm
│   │   │   │   ├── MessagingService.java       # Two-way messaging engine
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── ReportExportService.java    # CSV report generator
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   └── NulsfApplication.java          # Spring Boot main entry
│   │   └── resources/
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── swiss-nu.css           # Swiss Minimalist & NU theme styles
│   │       ├── templates/
│   │       │   ├── fragments/
│   │       │   │   └── nav.html               # Reusable Swiss navbar & footer
│   │       │   ├── AdminDashboard.html        # Comprehensive admin suite
│   │       │   ├── Catalog.html               # 12-col repository catalog
│   │       │   ├── ClaimPass.html             # Printable official release slip
│   │       │   ├── Home.html                  # Landing page
│   │       │   ├── ItemDetail.html            # Item inspector & smart matches
│   │       │   ├── login.html                 # Dual signup & sign-in
│   │       │   ├── Messages.html              # Split-pane messaging center
│   │       │   ├── MyClaims.html              # Student claims tracker
│   │       │   ├── Notifications.html         # Alerts center
│   │       │   ├── Profile.html               # Account profile
│   │       │   └── Report.html                # Lost vs Found reporting wizard
│   │       └── application.properties
├── pom.xml
└── README.md
```

---

<div align="center">

**National University Philippines &bull; Student Safety &amp; Security Office**  
*Built with ❤️ in pure Java Spring Boot for Bulldogs everywhere.*

</div>
