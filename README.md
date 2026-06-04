# SmartStay — Lodgings & Restaurant Management System

Welcome to SmartStay — a complete Spring Boot application that combines lodging (hotel) management and restaurant ordering into a single, modern web interface. This repository contains the source code, templates and static assets to run the system locally or on a server.

This README is written to help you get the project running quickly, understand the architecture, and contribute.

Highlights
- Full-featured admin UI (rooms, bookings, billing, reports, staff)
- Guest-facing token-based portal for ordering and billing
- Restaurant ordering integrated with guest stays
- Thymeleaf templates + Spring Boot controllers
- Easy to run with Gradle

Table of Contents
- Project overview
- Quick start (local)
- Configuration
- Key routes & API
- Development tips
- Packaging & deployment
- Contribution & license

Project overview
This project uses Spring Boot (Java) with Thymeleaf templates and a relational database (PostgreSQL recommended). The app serves both admin-facing pages and a token-based guest portal for ordering food and viewing stays.

Quick start (local development)
1. Prerequisites
   - Java 17+ installed
   - Gradle wrapper is included (no global Gradle needed)
   - PostgreSQL (or another supported RDBMS)

2. Configure the database
   - Create a database named `smartstay` (or choose a name and update properties)
   - Edit `src/main/resources/application.properties` and set your DB URL, username and password

3. Build and run
   ```powershell
   # from repository root on Windows PowerShell
   .\gradlew.bat clean build
   .\gradlew.bat bootRun
   ```

4. Open the app
   - Visit http://localhost:8080/ in your browser

Default admin credentials (development/demo)
- Email: admin@smartstay.com
- Password: Admin@123

Configuration
- Application configuration lives in `src/main/resources/application.properties`.
- Common settings to check:
  - `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
  - `server.port` (default 8080)
  - Any API keys (e.g. Groq AI) — keep secrets out of source control and use environment variables or a secrets manager in production.

Key routes
- `/` — Landing page
- `/dashboard` — Admin dashboard
- `/rooms` — Rooms management UI and API
- `/bookings` — Bookings UI and API
- `/restaurant` — Restaurant orders UI
- `/billing` — Billing UI
- `/reports` — Reports UI

Guest portal (token-based)
- `/guest/demo/portal` — Demo guest portal (no token)
- `/guest/{token}/portal` — Guest portal for a real token
- `/guest/{token}/order-food` — Place food orders
- `/guest/{token}/my-bill` — View bill
- `/guest/{token}/my-stay` — Stay details

API (examples)
- `GET /api/rooms` — list rooms
- `POST /api/rooms` — create room
- `PUT /api/rooms/{id}` — update room
- `DELETE /api/rooms/{id}` — delete room

Development tips
- Use the included Gradle wrapper (`gradlew.bat`) so everyone builds with the same Gradle version.
- IntelliJ IDEA users: there's an `.iml` and standard project structure; prefer importing the Gradle project.
- To run tests: `./gradlew test` (or `.\\gradlew.bat test` on Windows)

Packaging & deployment
- Build a runnable JAR with `./gradlew clean build` and find the artifact in `build/libs`.
- In production, run with appropriate environment variables and a production-grade database.

Contribution
- If you'd like to contribute, please fork the repository and open a pull request.

Security & secrets
- Do not commit API keys, passwords or other secrets. Use environment variables or a secure vault.

Contact / Credits
- Project: SmartStay
- Maintainer: (you) — repo: https://github.com/shawnie117/SmartStay_Lodgings_Restaurant_Management_System

Enjoy using SmartStay — if you'd like, I can also help you create a trimmed repository state that only contains the code, a clean `README.md` and a `.gitignore` (and push it to your GitHub repo). I will proceed with that now.
