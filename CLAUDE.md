# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

NetHelt is a network monitoring platform. It is a monorepo of four parts that talk to each other over HTTP:

- `web-api` – Spring Boot 4 / Java 25 backend (PostgreSQL, Flyway, JWT + OAuth2).
- `desktop-client` – Java agent installed inside a monitored network. It pulls monitoring tasks from `web-api`, runs them and reports back.
- `frontend` – Angular 21 web app for users.
- `ai-model` – Python (Poetry, Isolation Forest) anomaly detection on ping RTT statistics aggregated in 1-minute windows. Currently a CLI, meant to become an HTTP API.

Maven modules are `desktop-client/*` and `web-api` (root `pom.xml`); `frontend` and `ai-model` are built separately. Java 25 is required (`java.version` in the root pom).

## Commands

Run from the repo root. `Makefile` wraps most of them.

```bash
mvn clean verify -Dspring.profiles.active=test   # all Java tests (this is what CI runs; `make test`)
mvn clean install -pl web-api -am                # build one module with its dependencies
make dist                                        # desktop client app image + archive for the current OS (`-Pdist`)
mvn test -pl web-api -Dtest=ClassName#method -Dspring.profiles.active=test   # single test
mvn verify -Pcoverage -Dspring.profiles.active=test   # JaCoCo aggregate (coverage-aggregator module only exists in this profile)
mvn spotless:apply        # format Java (google-java-format); CI runs `spotless:check` on PRs
make lint                 # spotless + prettier + `ng lint --fix` for the frontend
make up / down / logs     # Docker Compose stack in infrastructure/ (needs infrastructure/.env, see .env.example)
```

Frontend (`cd frontend`): `npm install`, `ng serve`, `ng test`, `ng lint`.
AI model (`cd ai-model`): `poetry install`, then `poetry run python -m ai_model --mode train|predict ...` (see `ai-model/README.md` for flags).

Java tests run with the `test` profile: `web-api` uses in-memory H2, `ddl-auto=create-drop` and Flyway disabled (`application-test.properties`), whereas production uses PostgreSQL with `ddl-auto=validate` and Flyway migrations. So a new entity needs a migration in `web-api/src/main/resources/db/migration` (`V<major>.<minor>.<patch>__name.sql`) that matches the entity, because H2 tests will not catch a mismatch.

## Architecture

### web-api

- Package-by-feature under `pl.sgorski.nethelt.webapi.features/<feature>/` with `controller`, `domain`, `dto/{command,request,response}`, `mapper` (MapStruct), `repository`, `service`. Requests map to a `command` at the controller boundary; services work with commands and domain entities.
- API versioning is header-based (`X-API-Version`, default `1.0.0`); controllers declare `@RequestMapping(value = "...", version = "1")`.
- There are two audiences with separate auth, both configured in `config/SecurityConfig.java`:
  - Users: JWT access tokens (`AccessTokenAuthenticationFilter`), plus OAuth2 login (Google/GitHub).
  - Desktop agents: `Authorization: Agent <token>` (`AgentAccessTokenAuthenticationFilter`). Everything under `/client/**` requires the `AGENT` authority, and `AgentAuthentication.getPrincipal()` carries `agentId` and `networkId`. Agent endpoints are in `*DesktopController` classes (e.g. `MonitoringTaskDesktopController`), user-facing ones in the plain `*Controller`. Every other path is `denyAll`, so a new endpoint must be added to the matchers.
- Domain: `User` → `Network` → `Agent` (one per network) and `Device` → `MonitoringTask` (type `PING`/`TELNET`/`HTTP_HEALTHCHECK`, interval, enabled flag) → type-specific `MonitoringTaskConfiguration` subclass (own table per type).

### desktop-client

Modules: `client-core` (machine-wide PAT file shared by the CLI and the service, see `FileCredentialsStore`), `bg-client` (the monitoring service), `cli-client` (picocli commands like `auth register|status|remove`, which manage the stored PAT), `gui-client` (placeholder).

Agent auth: the user registers a PAT via the CLI, `TokenProvider` exchanges it at `/client/agent/authenticate` for an agent access token, and `AuthorizationInterceptor` adds it to every request to the `web-api` HTTP service group. Without a PAT the interceptor throws `MissingCredentialsException` and the schedulers skip the tick with a warning.

Packaging: `bg-client` and `cli-client` build Spring Boot executable jars with the `exec` classifier (the plain jar stays the main artifact). The `distribution` module (only in the `dist` profile) copies them as `nethelt-agent.jar`/`nethelt.jar` and runs jpackage to build one `app-image` with a shared jlink runtime and two launchers, `nethelt` (CLI) and `nethelt-agent`, then archives it as `nethelt-core-<os>-<arch>`. jpackage puts all jars on every launcher's classpath, which is fine only because each Spring Boot fat jar keeps its classes in `BOOT-INF`. Files of a launcher are named after it, so optional components are split by name. The JDK module list is `jlink.modules` in `distribution/pom.xml`.

bg-client runtime flow (spans several classes):
1. `TaskUpdateScheduler` polls `GET /client/monitoring-tasks` every `scheduler.update-interval-seconds`, and `HeartbeatScheduler` posts a heartbeat.
2. `MonitoringTaskService.synchronize` diffs the fetched tasks against the running ones (key = `deviceId` + task `id`, change detection via `updatedAt`) and adds, reschedules or cancels them through `ScheduledTaskManager` (`scheduleWithFixedDelay`, so `interval` is the delay between the end of one run and the start of the next).
3. On each tick the `MonitoringTaskHandler` matching the `TaskType` runs the task (`executor/handler/impl`), which uses the `NetworkOperation`s in `network/`.

HTTP clients are declarative Spring `@HttpExchange` interfaces registered in `webclient/config/HttpClientConfig` (`@ImportHttpServices`, group `web-api`). Base URL is set at build time from the `nethelt.api-url` Maven property (resource filtering of `@nethelt.api-url@`, default `http://localhost:8080/api`), users cannot change it. Non-2xx responses become `WebClientException` via `WebApiResponseErrorHandler`.

## Conventions

- Format with google-java-format via Spotless (2-space indent). Run `mvn spotless:apply` before committing; the PR check fails otherwise.
- Lombok is used throughout (`@RequiredArgsConstructor`, `@Getter`, `@Slf4j`); MapStruct annotation processing is configured in the root pom.
- Branches are named `net-<ticket>-<slug>`; commits are prefixed `NET-<ticket>: ...`.
- `.claude/settings.json` denies reading `.env*`, keys/keystores and `application-local.*`/`application-prod.*`, and denies `curl`/`wget`.
