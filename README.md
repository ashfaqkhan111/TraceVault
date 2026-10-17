# TraceVault

**Digital evidence. Clearer insights.**

TraceVault is an AI-assisted digital forensics web application designed to help users inspect digital evidence, identify potentially suspicious indicators, and organize analysis findings into understandable reports.

Built by a team of four Informatics students for **Arbiter Hacks V1**, TraceVault aims to make common digital-evidence inspection tasks more accessible while keeping findings transparent and tied to observable evidence.

> **Project status:** Hackathon project in development. Features described as planned or in progress should not be considered available until implemented and tested.

## Table of Contents

- [Overview](#overview)
- [Goals](#goals)
- [Planned Features](#planned-features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Repository Structure](#repository-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Development Workflow](#development-workflow)
- [API Plan](#api-plan)
- [Security and Responsible Use](#security-and-responsible-use)
- [Team Collaboration](#team-collaboration)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

## Overview

Digital evidence can contain useful metadata and indicators, but interpreting it can be time-consuming. TraceVault is intended to provide a clear workflow for uploading supported evidence files, running forensic checks, reviewing findings, and exporting a report.

TraceVault is an **investigation aid**, not a replacement for a qualified forensic examiner. Automated indicators and AI-generated explanations require human review and should not be treated as definitive proof that a file is safe, malicious, authentic, or fraudulent.

## Goals

- Provide a straightforward interface for submitting evidence for analysis.
- Calculate cryptographic hashes to help identify files and support integrity workflows.
- Extract relevant file metadata and inspect supported email artifacts.
- Present suspicious indicators with explanations grounded in the actual analysis results.
- Save analysis history and make reports available for later review.
- Encourage careful, repeatable, and responsible evidence handling.

## Planned Features

Implementation status should be updated by the team as work is completed and tested.

- [ ] User registration, login, and account settings
- [ ] Evidence upload with file-type and size validation
- [ ] SHA-256 file hashing
- [ ] Metadata extraction for supported file types
- [ ] Email artifact inspection for supported formats
- [ ] Detection and display of suspicious indicators
- [ ] AI-assisted explanations grounded in recorded findings
- [ ] Persistent analysis history
- [ ] Downloadable PDF reports
- [ ] Responsive dashboard and analysis-results interface
- [ ] Automated tests and deployment configuration

### Intended workflow

1. **Upload:** The user submits a supported evidence file.
2. **Analyze:** The backend calculates a hash and runs the applicable metadata and forensic checks.
3. **Review:** The user inspects findings, indicators, and their explanations.
4. **Report:** The user reviews or exports a report and can revisit saved analysis history.

Supported formats and file-size limits must be confirmed against the implemented backend before relying on them.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | HTML, CSS, vanilla JavaScript |
| Backend | Java with Spring Boot |
| Build tool | Maven |
| Database | PostgreSQL (planned) |
| Persistence | Spring Data JPA (planned) |
| Database migrations | Flyway (planned) |
| Forensic utilities | SHA-256 and appropriate file-parsing libraries |
| Version control | Git and GitHub |

The exact versions and dependencies should be recorded once the project setup is finalized.

## Architecture

TraceVault is planned as a frontend/backend application:

- **Frontend:** Provides authentication screens, the dashboard, upload flow, analysis results, history, and settings.
- **REST API:** Receives validated requests and returns structured responses.
- **Services:** Coordinate hashing, metadata extraction, email inspection, indicator generation, and report creation.
- **Persistence layer:** Stores users, analysis records, findings, and report metadata.
- **Database:** PostgreSQL is the planned persistent store.

The frontend must not be treated as a security boundary. The backend is responsible for validating uploads, enforcing access controls, and checking that a user is authorized to access a particular analysis.

## Repository Structure

The intended structure is:

```text
TraceVault/
├── frontend/
│   ├── index.html
│   ├── pages/
│   ├── css/
│   ├── js/
│   └── assets/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/tracevault/backend/
│       │   └── resources/
│       └── test/
├── docs/
│   ├── architecture.md
│   ├── api-specification.md
│   └── database-design.md
├── .env.example
├── .gitignore
└── README.md
```

This is the target layout; adjust it to match the actual files in the repository. Do not create duplicate folders just to match this diagram.

## Getting Started

### Prerequisites

Install the following tools:

- Git
- Java Development Kit (JDK) compatible with the Spring Boot version selected for the project
- Maven, if you are not using the Maven wrapper included in the generated project
- A modern web browser
- PostgreSQL when database integration is ready

Check your Java installation:

```bash
java -version
```

Check Git:

```bash
git --version
```

### 1. Clone the repository

SSH (recommended after configuring an SSH key with your GitHub account):

```bash
git clone git@github.com:ashfaqkhan111/TraceVault.git
cd TraceVault
```

Alternatively, use HTTPS:

```bash
git clone https://github.com/ashfaqkhan111/TraceVault.git
cd TraceVault
```

### 2. Run the backend

Once the Spring Boot project is in `backend/`, run:

```bash
cd backend
```

If the generated project includes the Maven wrapper on Linux:

```bash
./mvnw spring-boot:run
```

Otherwise, if Maven is installed:

```bash
mvn spring-boot:run
```

The default Spring Boot port is usually `8080`, unless configured differently. Check the application logs for the actual startup status and port.

> If Spring Security is included but has not yet been configured, it may protect endpoints by default. Configure security intentionally rather than disabling it in a production deployment.

### 3. Run the frontend

The frontend is planned as plain HTML, CSS, and JavaScript. During development, serve the `frontend/` directory using a local static HTTP server or your editor's development server. Avoid opening pages directly with `file://` if the application relies on API requests or browser modules.

Configure the frontend API base URL to match the backend address and port used in your local environment.

### 4. Database setup

PostgreSQL integration is planned. Once the team has added the database configuration and migrations:

1. Create a local database for TraceVault.
2. Configure the connection using local environment variables or an untracked configuration file.
3. Apply the project's Flyway migrations, if configured.
4. Start the backend and verify the connection in the logs.

Exact database names, credentials, and environment variable names must follow the application's actual configuration. Never commit real credentials to Git.

## Configuration

Use environment variables or local, untracked configuration for secrets and machine-specific settings.

An example `.env.example` may document placeholder values such as:

```dotenv
# Example placeholders only — adapt names to the backend configuration.
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tracevault
DB_USERNAME=change_me
DB_PASSWORD=change_me
```

This file is documentation, not a guarantee that Spring Boot automatically loads these variables. The team must map the variable names to the application's configuration.

- Commit `.env.example` with placeholder values only.
- Keep `.env` and any file containing real credentials out of version control.
- Never commit API keys, private SSH keys, real evidence files, or sensitive reports.

## Development Workflow

Use feature branches and pull requests so `main` remains stable.

1. Update your local `main` branch.
2. Create a branch for one focused task.
3. Implement and test the change.
4. Commit with a clear message.
5. Push the branch and open a pull request.
6. Request a teammate's review before merging.

Example:

```bash
git switch main
git pull origin main
git switch -c feature/sha256-analysis

# Make changes and run relevant tests.

git add .
git commit -m "Add SHA-256 analysis service"
git push -u origin feature/sha256-analysis
```

Suggested branch names:

- `feature/frontend`
- `feature/backend`
- `feature/database`
- `feature/authentication`
- `fix/<short-description>`
- `docs/<short-description>`

Agree on API request/response formats before frontend and backend work proceeds independently.

## API Plan

These endpoints are a proposed contract and are not guaranteed to be implemented yet.

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/analyses` | Submit a file for analysis |
| `GET` | `/api/analyses` | List the authenticated user's analyses |
| `GET` | `/api/analyses/{id}` | Retrieve a saved analysis and findings |
| `GET` | `/api/analyses/{id}/report` | Download or retrieve a report |
| `POST` | `/api/analyses/{id}/retry` | Retry an eligible failed analysis (optional) |

All endpoints that return private analysis data must enforce authentication and ownership/authorization checks. Define upload limits, error formats, and asynchronous processing behavior in `docs/api-specification.md` before the frontend depends on them.

## Security and Responsible Use

TraceVault is intended for lawful analysis of files that users own or are authorized to examine.

- **Treat uploads as untrusted input.** Validate file size, allowed formats, and content; do not rely only on the filename or browser-side checks.
- **Do not execute uploaded files.** Parse evidence in a controlled environment and keep original evidence read-only.
- **Protect evidence and reports.** Store them outside publicly served static directories and enforce access controls.
- **Handle sensitive data carefully.** Avoid logging file contents, credentials, personal data, or unnecessary metadata.
- **Use safe temporary storage.** Apply access restrictions and a clear retention/deletion policy.
- **Keep AI explanations grounded.** Clearly distinguish observed facts, heuristic indicators, and generated interpretations.
- **Avoid absolute verdicts.** A hash identifies bytes; by itself it does not prove integrity unless compared with a trusted reference hash. An indicator is not conclusive proof of malicious activity.
- **Test responsibly.** Prefer synthetic or authorized test artifacts and do not upload private evidence to third-party AI services without explicit authorization and a suitable privacy review.

## Team Collaboration

TraceVault is being developed by a four-person Informatics student team for Arbiter Hacks V1.

Use GitHub Issues or a project board to track tasks. Keep pull requests focused, document decisions, and review changes before merging. Assign owners for frontend, forensic analysis, persistence/authentication, and integration/testing, while sharing responsibility for security and code review.

## Roadmap

- [ ] Finalize repository structure and development setup
- [ ] Agree on REST API contracts and database schema
- [ ] Start the Spring Boot application locally
- [ ] Build the initial frontend shell and upload experience
- [ ] Implement file validation and SHA-256 hashing
- [ ] Add metadata and supported email artifact inspection
- [ ] Persist analyses and findings
- [ ] Build the results and history views
- [ ] Generate downloadable PDF reports
- [ ] Add authentication, authorization, and automated tests
- [ ] Prepare a demo using synthetic or authorized test evidence

## Contributing

Contributions from project teammates are welcome.

- Create a feature branch for each task.
- Follow existing code style and naming conventions.
- Add or update tests where practical.
- Update documentation when behavior or API contracts change.
- Never include secrets, personal data, or real evidence in commits.
- Open a pull request and request review before merging.

## License

No license has been specified yet. Unless and until a license is added to this repository, do not assume that the project is licensed for reuse, redistribution, or modification by the public. Add a `LICENSE` file once the team has agreed on the appropriate license.
