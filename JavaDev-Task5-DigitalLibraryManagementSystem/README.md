# Digital Library Management System

Task 5 of the Java Development internship: a web-based library with Admin and User roles, built with Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA, Thymeleaf, MySQL, HTML and CSS.

## Feature checklist

### Admin module
| Requirement | Where |
|---|---|
| Admin login with access to all admin features | `/login`, then `/admin` |
| Add a book (title, author, ISBN, category, quantity) | `/admin/books/new` |
| Edit or delete a book | `/admin/books` |
| View issued books and due dates | `/admin/loans` |
| View and manage members | `/admin/members` (loan history, disable / enable) |
| Fine management: mark fines as paid | `/admin/fines` |
| Read contact messages | `/admin/messages` |

### User module
| Requirement | Where |
|---|---|
| Registration and login | `/register`, `/login` |
| Email verification code on sign-up | `/verify` |
| Forgot password with an emailed code | `/forgot-password`, `/reset-password` |
| Browse by category, search by title or author | `/books` |
| Issue and return books | `/books`, `/my/loans` |
| Automatic fine for late returns (5 per day) | on return |
| Advance booking of a book issued to someone else | `/books`, `/my/reservations` |
| Contact form stored in the database | `/contact`, read by admin in `/admin/messages` |

## Email codes
- **Sign-up:** a new account is created unverified and a 6-digit code is emailed. The member cannot sign in until the code is entered at `/verify`. Trying to sign in early sends them to that page. Accounts that existed before this feature, and the admin, count as verified.
- **Password reset:** `/forgot-password` emails a code, and `/reset-password` takes the code plus a new password. The page never reveals whether an email has an account.
- **Code rules:** 6 digits, valid 10 minutes, single use, stored only as a BCrypt hash, 5 wrong attempts burn the code, one new code per 60 seconds. Change these in `application.properties` (`library.verification.*`).
- **Sending:** `library.mail.provider=log` (default) prints the email, including the code, in the console, so you can develop without an email account. `library.mail.provider=brevo` sends real email through Brevo's HTTPS API (free plan: 300 emails a day).

## Business rules
- Loan period 14 days, at most 5 active loans per member, fine = days late x 5.
- A member with an unpaid fine or an overdue book cannot borrow until it is cleared.
- A book can only be reserved when every copy is issued; returned copies are held for the longest-waiting reservations first.
- A book with loan history cannot be deleted. Quantity can never go below the number of copies currently issued.

All numbers are in `src/main/resources/application.properties` (`library.*`).

## Run locally
Needs JDK 21+, MySQL 8 and Maven (or IntelliJ).
1. Set your MySQL password in the `DB_PASSWORD` environment variable (IntelliJ: Run, Edit Configurations, Environment variables). `DB_USERNAME` defaults to `root`.
2. `mvn spring-boot:run`, or run `DigitalLibraryApplication`.
3. Open <http://localhost:8080>. The database, tables, the admin account and 12 sample books are created on first start.

Default admin: `admin@library.com` / `Admin@123`. With the default `log` mail provider, copy verification and reset codes from the console.

## Tests
```
mvn test
```
They run on in-memory H2, so MySQL is not needed. Coverage: fine maths, books, loans, reservations, members, contact messages, email codes (hashing, expiry, attempt limit, throttling, reset, single use), the Brevo sender (against a mock server), and full web flows through MockMvc with real security (login redirects, role separation, CSRF, sign-up verification, password reset, issue / return / fines, reservations, admin CRUD).

## Deploy for free (no card needed)
Three free services work together: **TiDB Cloud Starter** (MySQL-compatible database), **Brevo** (email over HTTPS) and **Render** (runs the Docker image). Render's free tier blocks SMTP ports, which is why email goes through Brevo's HTTPS API. Free tiers change, so check each site's current limits.

1. **Push the project to GitHub.** No passwords belong in the repo.
2. **Database (TiDB Cloud Starter).** Create a free Starter cluster, open Connect, generate a password, and note the host, port (4000), username (looks like `abc123.root`) and password. In the SQL editor run `CREATE DATABASE digital_library;`.
3. **Email (Brevo).** Create a free account. Under Senders add the email address you will send from and click the confirmation link in the email Brevo sends. Under SMTP & API create an API key.
4. **App (Render).** New, Web Service, pick the repo. If the repo holds several projects, set Root Directory to this folder. Choose Docker and the Free instance type. Add these environment variables:

| Variable | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://HOST:4000/digital_library?sslMode=VERIFY_IDENTITY&serverTimezone=UTC` |
| `DB_USERNAME` | the TiDB username |
| `DB_PASSWORD` | the TiDB password |
| `ADMIN_EMAIL` | your real email (so the admin can reset the password by code) |
| `ADMIN_PASSWORD` | a strong password for the first admin login |
| `MAIL_PROVIDER` | `brevo` |
| `BREVO_API_KEY` | the Brevo API key |
| `MAIL_FROM` | the sender address you verified in Brevo |
| `MAIL_FROM_NAME` | `The Reading Room` |
| `SESSION_COOKIE_SECURE` | `true` |

5. Deploy, then open the `onrender.com` address Render gives you. Tables and the admin account are created on first start.

Things to know about the free tier:
- The app sleeps after 15 minutes without visits and takes about a minute to wake up. A free uptime pinger hitting `/login` every 10 minutes keeps it awake, within Render's 750 free hours a month.
- The app has 512 MB of RAM and a fraction of a CPU, so the first start is slow.
- Data lives in TiDB, not on Render, so redeploying does not lose it.
- Mail sent from a free address such as Gmail may land in spam. A domain you own, verified in Brevo, avoids that.

## Layout
```
src/main/java/ng/sciCloud/library
  config/       security, properties, seeding
  controller/   member pages, auth and email-code pages, admin pages
  dto/          validated form objects
  mail/         EmailSender, LoggingEmailSender, BrevoEmailSender
  model/        entities and enums
  repository/   Spring Data repositories
  service/      business services (including VerificationService)
  utils/validators/  rules checked by the services
src/main/resources  templates, static/css, application.properties
src/test            unit and integration tests
Dockerfile          production image
database/schema.sql reference schema
```
