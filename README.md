# SISMS — Secure Inventory and Supply Chain Management System

Desktop application for managing inventory, suppliers, warehouses, orders, customers, and users.
Built with **Java Swing** for the GUI and **MySQL** as the database, with email-based **OTP two-factor authentication** and **BCrypt** password security.

---

## Table of Contents

- [Project Overview](#-project-overview)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Screens & Navigation](#-screens--navigation)
- [One-Time Installation](#️-one-time-installation)
- [Database Setup](#-database-setup)
- [Configuration](#-configuration)
- [Running the App](#️-running-the-app)
- [Roles & Permissions](#-roles--permissions)
- [Default Login](#-default-login)
- [API Reference (MySQL)](#-api-reference-mysql)
- [Database Programmability](#-database-programmability)
- [UI Component Library](#-ui-component-library)
- [Architecture](#-architecture)
- [Known Issues / Future Enhancements](#-known-issues--future-enhancements)

---

## 📋 Project Overview

SISMS allows inventory managers and admins to:

- Securely log in with email-based **OTP two-factor authentication** via Gmail SMTP, with automatic account lockout after repeated failed attempts
- Manage **products**, **categories**, **shelves**, **warehouses**, and **suppliers**, with full add / update / delete support on every screen
- Track the many-to-many relationship between **suppliers and warehouses**
- Manage **customer** contact records (SuperAdmin-managed) and **user accounts**
- Place and track **orders** with quantity validation, and auto-generate **PDF invoices** using iTextPDF
- View the full **order history** per customer
- See a live **dashboard** with KPIs and proactive **low-stock alerts**
- Role-based access control — SuperAdmin, Admin, and Customer roles each see different menus

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| GUI Framework | Java Swing |
| Database | MySQL 8+ |
| JDBC Driver | mysql-connector-j 9.5.0 |
| Password Hashing | jBCrypt 0.4 |
| PDF Generation | iTextPDF 5.5.13 |
| Email / OTP | Jakarta Mail + Jakarta Activation via Gmail SMTP (TLS, port 587) |
| Build / IDE | VS Code (Java Extension Pack) or IntelliJ IDEA |

---

## 📁 Project Structure

```
SISMS/
│
├── src/
│   ├── screens/                  # All GUI windows (route-level screens)
│   │   ├── Main.java             # ← Entry point, run this
│   │   ├── login.java            # Login screen + OTP trigger
│   │   ├── OTPVerification.java  # OTP input & verification window
│   │   ├── Home.java             # Main dashboard shell with sidebar
│   │   ├── Product.java          # Product CRUD screen (add/update/delete)
│   │   ├── Category.java         # Category CRUD screen (case-insensitive dedup)
│   │   ├── Warehouse.java        # Warehouse CRUD screen
│   │   ├── Supplier.java         # Supplier CRUD screen + warehouse linking
│   │   ├── Order.java            # Order placement + PDF invoice
│   │   ├── ViewOrders.java       # Order history viewer
│   │   ├── Customer.java         # Customer CRUD screen (SuperAdmin only)
│   │   └── User.java             # User management (SuperAdmin only)
│   │
│   ├── dao/                      # Data Access Layer
│   │   ├── ConnectionProvider.java   # MySQL connection factory (reads AppConfig)
│   │   ├── tables.java               # One-time DB schema setup & seed
│   │   ├── InventoryUtils.java       # Cross-platform PDF output path
│   │   └── TestConnection.java       # Quick DB connectivity test
│   │
│   ├── common/                   # Shared utilities
│   │   ├── EmailService.java     # Reusable SMTP email sender (reads AppConfig)
│   │   ├── OTPGenerator.java     # Cryptographically secure 6-digit OTP
│   │   └── OpenPdf.java          # Cross-platform PDF opener (java.awt.Desktop)
│   │
│   ├── ui/                       # UI component library
│   │   ├── UIStyle.java          # Centralized colors, fonts, spacing constants
│   │   ├── RoundedPanel.java     # Custom JPanel with rounded corners + shadow
│   │   ├── ModernButton.java     # Styled JButton with hover effects
│   │   ├── ModernTableRenderer.java  # Alternating-row table cell renderer
│   │   └── ErrorHandler.java     # Standardized error dialog helper
│   │
│   └── config/
│       ├── AppConfig.java.example  # Boilerplate — copy this, see Configuration below
│       └── AppConfig.java          
│
├── resources/
│   └── images/                   # Background and icon assets used by the UI
│
├── lib/                          # All dependency JARs
│   ├── mysql-connector-j-9.5.0.jar
│   ├── jbcrypt-0.4.jar
│   ├── jakarta.mail-2.0.1.jar
│   ├── jakarta.activation-2.0.1.jar
│   └── itextpdf-5.5.13.3.jar
│
├── DB_SETUP.sql                  # Tables, constraints, and seed data (run this first)
├── DB_PROCEDURES.sql             # Functions, procedures, triggers, views (run this second)
├── .gitignore
└── README.md
```

---

## 🖥 Screens & Navigation

| # | Screen | Class | Description |
|---|---|---|---|
| 1 | Login | `login.java` | Email + password login, triggers OTP email on success |
| 2 | OTP Verification | `OTPVerification.java` | 6-digit OTP entry, 10-minute expiry, redirects to Home |
| 3 | Dashboard | `Home.java` | KPI cards, low-stock alerts, sidebar navigation |
| 4 | Products | `Product.java` | Add / update / delete products with category, shelf, and supplier |
| 5 | Categories | `Category.java` | Add / update / delete categories, duplicate names blocked case-insensitively |
| 6 | Warehouses | `Warehouse.java` | Add / update / delete warehouses |
| 7 | Suppliers | `Supplier.java` | Add / update / delete suppliers, link/unlink to warehouses |
| 8 | Orders | `Order.java` | Place orders, quantity validated against stock, generates PDF invoice |
| 9 | View Orders | `ViewOrders.java` | Full order history per customer |
| 10 | Customers | `Customer.java` | Add / update / delete customer records — SuperAdmin only |
| 11 | Users | `User.java` | Manage user accounts and roles — SuperAdmin only |

All screens after Login are accessible only when authenticated. Role-based visibility is enforced on the sidebar — Customers only see Dashboard, Orders, and View Orders.

---

## ⚙️ One-Time Installation

These steps only need to be done once on a new machine.

### Prerequisites

- [Java JDK 17+](https://adoptium.net/)
- [MySQL 8.0+](https://dev.mysql.com/downloads/)
- [VS Code](https://code.visualstudio.com/) with the **Java Extension Pack** installed, **or** IntelliJ IDEA

### Clone the project

```
git clone https://github.com/nabia-faisal/SMART-INVENTORY-MANAGEMENT-SYSTEM.git
cd SMART-INVENTORY-MANAGEMENT-SYSTEM
```

Then open the folder in VS Code (File → Open Folder) or IntelliJ (File → Open).

### Add JARs to classpath

All JARs are already in `lib/`. Your IDE should pick them up automatically via `.vscode/settings.json`. If not:

**VS Code** — `.vscode/settings.json` already contains:
```json
{
    "java.project.sourcePaths": ["src"],
    "java.project.referencedLibraries": ["lib/**/*.jar"]
}
```

**IntelliJ** — File → Project Structure → Modules → Dependencies → `+` → JARs → select all files in `lib/`

---

## 🗄 Database Setup

### 1. Create the database

Open MySQL Workbench or any MySQL client and run:

```sql
CREATE DATABASE inventory;
```

### 2. Run the schema script

Open `DB_SETUP.sql` in your MySQL client and run the whole file. This creates all tables, constraints, and inserts the default SuperAdmin account:

```sql
SOURCE /path/to/SISMS/DB_SETUP.sql;
```

Tables created:

| Table | Purpose |
|---|---|
| `appuser` | Login accounts (SuperAdmin / Admin / Customer), BCrypt passwords, OTP fields |
| `category` | Product categories (unique name constraint) |
| `warehouse` | Physical storage locations |
| `shelf` | Shelves, each belonging to one warehouse |
| `supplier` | Vendor / supplier contacts |
| `supplier_warehouse` | Junction table — which suppliers serve which warehouses |
| `product` | Stock items, referencing category, shelf, and supplier |
| `customer` | Customer contact records, managed manually by SuperAdmin |
| `orderDetail` | Order records linked to a customer |

### 3. Run the functions / procedures / triggers / views script

Open `DB_PROCEDURES.sql` and run the whole file — it adds all stored functions, procedures, triggers, and views used by the reporting and stock-tracking features. See [Database Programmability](#-database-programmability) below for what each one does.

```sql
SOURCE /path/to/SISMS/DB_PROCEDURES.sql;
```

---

## 🔐 Configuration

`AppConfig.java` holds live database and email credentials, so it is **not** part of this repository — you create your own local copy from the provided template.

1. Copy the template:
   ```
   # macOS / Linux
   cp src/config/AppConfig.java.example src/config/AppConfig.java

   # Windows
   copy src\config\AppConfig.java.example src\config\AppConfig.java
   ```
2. Open `src/config/AppConfig.java` and fill in your own values:

   ```java
   // ─── Database ────────────────────────────────────────────────────────────
   public static final String DB_URL      = "jdbc:mysql://127.0.0.1:3306/inventory?useSSL=false";
   public static final String DB_USER     = "root";                 // ← your MySQL username
   public static final String DB_PASSWORD = "your_mysql_password";  // ← your MySQL password

   // ─── Email (Gmail SMTP) ──────────────────────────────────────────────────
   public static final String SMTP_HOST     = "smtp.gmail.com";
   public static final String SMTP_PORT     = "587";
   public static final String SMTP_USER     = "your_email@gmail.com";  // ← your Gmail
   public static final String SMTP_PASSWORD = "your_app_password";     // ← Gmail App Password
   ```

> **Gmail App Password** — this is NOT your Gmail login password. Generate one at
> [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
> (requires 2-Step Verification enabled on the Google account).

> ⚠️ `AppConfig.java` is listed in `.gitignore`. Never commit it or push it to a public repository — it will contain a real database password and a real Gmail app password.

---

## ▶️ Running the App

No terminals needed — this is a desktop app. Just run the main class from your IDE.

### In VS Code

1. Open `src/screens/Main.java`
2. Click the **▷ Run** button above the `main` method, or press `F5`

### In IntelliJ IDEA

1. Open `src/screens/Main.java`
2. Click the green **▶ Run** button in the gutter next to `main`, or press `Shift+F10`

### From the command line

```
# From the project root, on Windows
javac -cp "lib/*" -d bin src/config/*.java src/dao/*.java src/common/*.java src/ui/*.java src/screens/*.java
java  -cp "bin;lib/*" screens.Main
```

> On macOS / Linux use `:` instead of `;` in the classpath.

Expected output in the console:

```
MySQL connection established successfully.
```

Then the login window opens. On first run, log in with the default SuperAdmin account (see below).

---

## 👥 Roles & Permissions

| Feature | SuperAdmin | Admin | Customer |
|---|:---:|:---:|:---:|
| Dashboard | ✅ | ✅ | ✅ |
| Products | ✅ | ✅ | ❌ |
| Categories | ✅ | ✅ | ❌ |
| Warehouses | ✅ | ✅ | ❌ |
| Suppliers | ✅ | ✅ | ❌ |
| Orders | ✅ | ✅ | ✅ |
| View Orders | ✅ | ✅ | ✅ |
| Customers | ✅ | ❌ | ❌ |
| User Management | ✅ | ❌ | ❌ |

---

## 🔑 Default Login

After running `DB_SETUP.sql`, the following SuperAdmin account is seeded automatically:

| Field | Value |
|---|---|
| Email | `superadmin@testemail.com` |
| Password | `admin` |
| Role | SuperAdmin |

> ⚠️ Change this password immediately after first login via the User Management screen.

---

## 🗃 API Reference (MySQL)

SISMS communicates directly with MySQL via JDBC — there is no REST API layer. All queries go through `dao/ConnectionProvider.java`. Key operations per module:

### Auth (`login.java`, `OTPVerification.java`)

| Operation | Table | Description |
|---|---|---|
| Verify credentials | `appuser` | Checks email + BCrypt password hash |
| Track failed attempts | `appuser` | Increments `failedAttempts`; locks account at 3 |
| Lock account | `appuser` | Sets `status = 'Inactive'`, sends lockout email |
| Store OTP | `appuser` | Writes `otp` + `otpExpiry` (10 min) on login success |
| Verify OTP | `appuser` | Checks OTP value and expiry timestamp |
| Clear OTP | `appuser` | Nulls `otp` + `otpExpiry` after successful verification |

### Products (`Product.java`)

| Operation | Table | SQL |
|---|---|---|
| Load table | `product` | `SELECT * FROM product` |
| Add | `product` | `INSERT INTO product (name, quantity, price, category_fk, shelf_fk, supplier_fk, ...)` |
| Update | `product` | `UPDATE product SET ... WHERE product_pk=?` |
| Delete | `product` | `DELETE FROM product WHERE product_pk=?` |

### Categories, Warehouses, Suppliers

| Operation | Table | Notes |
|---|---|---|
| Add / rename | `category` | Blocked if a case-insensitive duplicate already exists |
| Delete | `category`, `warehouse`, `supplier` | Dependent products are set to "unassigned" (`ON DELETE SET NULL`), not deleted |
| Link / unlink | `supplier_warehouse` | Composite key `(supplier_fk, warehouse_fk)` |

### Orders (`Order.java`)

| Operation | Tables | Description |
|---|---|---|
| Place order | `orderDetail`, `product` | Validates quantity is positive, inserts order, decrements product quantity |
| Generate invoice | — | Creates PDF bill via iTextPDF, saves to `~/sisms_bills/` |
| Open invoice | — | Opens PDF with `java.awt.Desktop.open()` |

### Users (`User.java`)

| Operation | Table | Description |
|---|---|---|
| Load users | `appuser` | SuperAdmin sees all; Admin sees non-SuperAdmin only |
| Add user | `appuser` | Saves with BCrypt-hashed password |
| Update user | `appuser` | Updates all fields; re-hashes if password changed |
| Delete user | `appuser` | SuperAdmin only; SuperAdmin accounts cannot be deleted |

---

## 🧮 Database Programmability

Defined in `DB_PROCEDURES.sql`.

**Functions**

| Function | Returns | Purpose |
|---|---|---|
| `fun_getStockValue(product_id)` | DECIMAL | Price × quantity for one product |
| `fun_getWarehouseCountForSupplier(supplier_id)` | INT | How many warehouses a supplier delivers to |
| `fun_stockStatus(quantity)` | VARCHAR | Classifies a quantity as Out of Stock / Low / Medium / High |

**Procedures**

| Procedure | Parameters | Purpose |
|---|---|---|
| `sp_restock_product` | IN, IN | Adds stock to a product, rejects non-positive quantities |
| `sp_get_low_stock_products` | IN | Lists products under a given threshold |
| `sp_get_inventory_value` | OUT | Total value of all stock on hand |
| `sp_transfer_product_shelf` | IN, IN, OUT | Moves a product to a different shelf |
| `sp_apply_bulk_discount` | IN, INOUT | Applies a discount across a category, capped at 50% |
| `sp_generate_shelves` | IN, IN | Auto-generates N shelves for a warehouse |
| `sp_get_supplier_stock_value` | IN, OUT | Total stock value sourced from one supplier |

**Triggers** — all on `product`, writing to a `product_log` audit table

| Trigger | Fires |
|---|---|
| `trg_before_insert_product` | BEFORE INSERT |
| `trg_after_insert_product` | AFTER INSERT |
| `trg_before_update_product` | BEFORE UPDATE |
| `trg_after_delete_product` | AFTER DELETE |

**Views**

| View | Purpose |
|---|---|
| `low_stock_vu` | Products under 20 units |
| `product_catalog_vu` | Full product → category → shelf → warehouse → supplier join |
| `supplier_warehouse_summary_vu` | Warehouse coverage per supplier |
| `top5_products_by_value_vu` | Top 5 products by stock value |
| `karachi_warehouse_vu` | Restricted view with `WITH CHECK OPTION` |
| `suppliers_without_products_vu` | Suppliers with no products yet (correlated subquery) |

---

## 🎨 UI Component Library

All visual constants and custom components are in `src/ui/`. No external UI library is used — everything is custom Swing.

| Component | File | Usage |
|---|---|---|
| Color palette, fonts, spacing | `UIStyle.java` | Imported by every screen — single source of truth |
| Rounded card panel | `RoundedPanel.java` | Dashboard KPI cards, form panels, table containers |
| Styled button with hover | `ModernButton.java` | Action buttons throughout the app |
| Alternating-row table | `ModernTableRenderer.java` | All `JTable` instances across every screen |
| Error dialog | `ErrorHandler.java` | Catches and displays exceptions consistently |

### Color Palette (`UIStyle.java`)

| Constant | Color | Used for |
|---|---|---|
| `PRIMARY` | `#001E3C` (very dark blue) | Sidebar background, button fills, table headers |
| `BACKGROUND` | `#FFFFFF` (white) | All window backgrounds |
| `CARD_BG` | `#E6F0FF` (light blue) | KPI cards, form panels |
| `TEXT_DARK` | `#2B2B2B` | Primary text |
| `TEXT_LIGHT` | `#808080` | Subtitles, placeholder labels |
| `SUCCESS` | `#4CAF50` | Success messages |
| `ERROR` | `#8B0000` (dark red) | Delete buttons, error alerts |

---

## 🏗 Architecture

SISMS follows a lightweight **MVC pattern** entirely within Java:

| Layer | Location | Responsibility |
|---|---|---|
| **Model** | `src/dao/` + MySQL | Schema definition, JDBC queries, data persistence |
| **View** | `src/screens/` + `src/ui/` | Swing JFrame windows and custom UI components |
| **Controller** | Logic inside each `screens/*.java` | Event handlers, form validation, DB calls, navigation |

### Java Patterns Used

| Pattern | Location | Description |
|---|---|---|
| Singleton connection | `ConnectionProvider.java` | One static `getCon()` method used everywhere |
| Static constants | `UIStyle.java` | All design tokens accessed as `UIStyle.PRIMARY` etc. |
| Lambda listeners | All screens | `btn.addActionListener(e -> methodName())` |
| CardLayout | `Home.java` | Switches between Dashboard panels without new windows |
| Role-based rendering | `Home.java` | Sidebar menu items shown/hidden based on `userRole` |
| BCrypt hashing | `login.java`, `User.java` | Passwords stored and verified with `jBCrypt` |
| OTP expiry check | `OTPVerification.java` | `LocalDateTime.now().isAfter(expiry)` |
| Cross-platform PDF open | `OpenPdf.java` | `java.awt.Desktop.getDesktop().open(file)` |

---

## 🚧 Known Issues / Future Enhancements

- Order line items aren't stored in their own table yet — a past order's exact product breakdown lives only in its generated PDF, not in a queryable SQL table.
- Stock intake is manual entry only; barcode scanning would speed this up.
- Low-stock alerts are dashboard-based rather than pushed by email.
- Desktop-only (Windows/Java); no mobile companion app.
