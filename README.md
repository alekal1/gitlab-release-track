[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/alekal1/gitlab-release-track)

# Release Management Desktop App

## Motivation

As a developer managing releases for multiple services, I was tracking release processes manually in a notepad — service names, versions, git hashes, pipeline statuses, and deploy steps. This became error-prone and tedious, especially on days when 10+ services needed to be released.

This desktop application replaces that manual workflow with a structured, day-based release tracker that integrates directly with GitLab to fetch project tags, commit hashes, and monitor pipeline statuses automatically.

## Technical Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **UI** | JavaFX 21 (programmatic, dark theme) |
| **Backend** | Spring Boot 3.4.4 (embedded, no web server) |
| **Persistence** | H2 Database (file-based, `./data/releaseapp.mv.db`) |
| **ORM** | Spring Data JPA / Hibernate |
| **HTTP Client** | Spring WebFlux WebClient (for GitLab API) |
| **Build** | Gradle 9.x |
| **Export** | OpenCSV 5.9 (CSV), plain FileWriter (Markdown) |
| **Other** | Lombok, JavaFX-Spring integration via custom event bridge |

## Setup & Configuration

### Prerequisites

- **Java 21+** installed and on `PATH`
- A **GitLab personal access token** with `api` scope

### 1. Clone the repository

```bash
git clone <repo-url>
cd releaseapp
```

### 2. Create the `.env` file

Create a `.env` file in the project root directory (**see .env.sample**):

```dotenv
GITLAB_BASE_URL=https://gitlab.example.com/
GITLAB_TOKEN=glpat-xxxxxxxxxxxxxxxxxxxx
GITLAB_PIPELINE_LAST_STEP=deploy-dev
```

| Variable | Description                                      |
|----------|--------------------------------------------------|
| `GITLAB_BASE_URL` | Your GitLab instance URL (no trailing `/api/v4`) |
| `GITLAB_TOKEN` | Personal access token with `read_api` scope      |
| `GITLAB_PIPELINE_LAST_STEP` | Tag pipeline last step                           |

> **Note:** The `.env` file is in `.gitignore` and will not be committed.

### 3. Run the application

```bash
./gradlew bootRun
```

On Windows:
```cmd
gradlew.bat bootRun
```

The application window will open with a dark-themed UI.
