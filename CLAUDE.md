# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

All Maven commands are run from the `source/` directory:

```bash
cd source

# Build JAR
mvn clean package

# Run locally (requires env vars set — see Configuration)
java -jar target/movie-api.jar

# Skip tests during build
mvn clean package -DskipTests

# Run with specific profile
java -jar target/movie-api.jar -Dspring.profiles.active=dev
```

Docker:
```bash
# Build image
docker build -f dev-ops/Dockerfile . --tag movie-api:1.0

# Run container
docker run -it -p 8383:8383 -e "SPRING_PROFILES_ACTIVE=dev" movie-api:1.0
```

## Database Migrations (Liquibase)

Schema migrations live in `source/src/main/resources/liquibase/`. The master changelog is `db.changelog-master.xml`.

Generate a diff changelog from current Hibernate models vs DB:
```bash
cd source
mvn liquibase:diff
```
The diff output goes to `src/main/resources/liquibase/<timestamp>_changelog.xml`.

## Configuration

The active profile is set in `source/src/main/resources/application.properties` (defaults to `local`). Profile-specific files are `application-{profile}.properties`.

The `local` profile (`application-local.properties`) reads all secrets from environment variables:

| Variable | Purpose |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `AUTH_SIGNING_KEY` | JWT signing key (shared with auth service) |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | RabbitMQ |
| `REDIS_HOST`, `REDIS_TYPE`, `REDIS_PASSWORD`, `REDIS_SENTINEL_HOSTS`, `REDIS_MASTER_NAME` | Redis |
| `MQTT_BROKER`, `MQTT_USERNAME`, `MQTT_PASSWORD`, `MQTT_CLIENT_ID` | MQTT (Mosquitto) |
| `ONE_SIGNAL_APP_ID`, `ONE_SIGNAL_API_KEY` | Push notifications |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP |
| `IMDB_ENABLE`, `IMDB_API_URL`, `IMDB_API_KEY` | OMDb ratings sync |
| `SERVER_INTERNAL_PASSWORD`, `AUTH_INTERNAL_PASSWORD` | Internal service auth |
| `PORT` | Server port (default 8383) |

## Architecture Overview

This is a Spring Boot 2.3 / Java 11 REST API (resource server) for a movie-streaming platform. It is **not** an auth server — it validates JWT tokens issued by a separate auth service.

### Layer structure

```
com.movie.api
├── cfg/           — Spring configuration (Security, MQTT, Redis, Swagger, async, Feign)
├── constant/      — BaseConstant, DatabaseConstant, SecurityConstant
├── controller/    — REST controllers, all extend ABasicController
├── dto/           — Response DTOs
├── exception/     — Custom exceptions + GlobalExceptionHandler
├── form/          — Request bodies (validated with Bean Validation)
├── jwt/           — BaseJwt: parses the custom JWT claim stored compressed/zipped
├── mapper/        — MapStruct mappers (entity ↔ DTO / form)
├── scheduler/     — Cron jobs (movie, notifications, recommendations, rooms)
├── service/       — Business logic interfaces + impls
│   ├── feign/     — OpenFeign clients to external services
│   ├── impl/      — Service implementations
│   ├── mqtt/      — MQTT outbound publisher
│   ├── rabbit/    — RabbitMQ listeners, sender, configuration
│   └── redis/     — Redis operations
├── storage/
│   ├── base/      — Auditable<T> base entity (auto id, createdBy/Date, modifiedBy/Date, status)
│   ├── model/     — JPA entities (table prefix: `db_`)
│   └── repository/— Spring Data JPA repositories
├── utils/         — Utility classes (AES, hash, date, string, JSON, zip)
└── validation/    — Custom JSR-303 constraint annotations + implementations
```

### Security

- Acts as an **OAuth2 Resource Server** (`ResourceServerConfig`). Tokens are JWT signed with a shared `auth.signing.key`.
- `CustomTokenConverter` extracts a compressed custom claim (`additionalInfo`) into `BaseJwt` via `ZipUtils`.
- `ABasicController` provides `getCurrentUser()`, `isAdmin()`, `isSuperAdmin()`, `isEmployee()`, `isUser()` helpers used across all controllers.
- Public endpoints (no JWT required) are listed in `SecurityConstant.ENDPOINTS_BYPASS_JWT`.
- Internal endpoints (`/v1/*/internal/**`) are public but protected by `server.internal.password` header validation.

### Account roles (userKind in JWT)

- `1` = Admin, `2` = Employee, `3` = User

### Domain models

Key entities and relationships:
- **Movie** (type: 1=single, 2=series) → **MovieItem** (kind: 1=season, 2=episode, 3=trailer) → **VideoLibrary** (source video)
- **Account** → **Review**, **Comment**, **Favourite**, **WatchHistory**, **Playlist**, **UserReport**
- **Comment** supports self-referential parent/child for replies
- **Room** + **Participant** for synchronized watch-party rooms (state managed via MQTT)
- **Collection** (type: 1=topic, 2=section) → **CollectionItem** for curated content groupings
- **UserMovie** tracks user interactions (survey, watched, disliked, favorite, playlist, review, watch-progress)
- **UserMovieScore** + **MovieSimilarity** feed the recommendation engine

### Messaging

- **RabbitMQ**: Receives async events on `UPDATE_VIDEO_QUEUE` (video processing done, subtitle done, toxic content detection results) and `ACCOUNT_SYNC_MOVIE_QUEUE` (account created/updated/deleted events from auth service). Sends to `CONVERT_VIDEO_QUEUE`, `TOXIC_COMMENT_DETECTOR_QUEUE`.
- **MQTT (Mosquitto)**: Real-time messaging for watch-party rooms and notification delivery. Inbound topic: `notification_in`; outbound: `notification`; room topics: `room/{id}`.
- **OneSignal**: Push notifications sent via Feign client for specific CMD types (new movie, reply, vote, toxic lock/unlock).

### External Feign clients

| Client | Purpose |
|---|---|
| `FeignAccountAuthService` | Internal auth service (token introspection, user lookup) |
| `FeignFileMediaService` | Media service (file upload/download) |
| `FeignOmdbService` | OMDb API for IMDB ratings sync |
| `FeignOneSignalService` | OneSignal push notification API |
| `FeignRecommendationService` | Python recommendation service (item-KNN, hybrid rebuild) |

### Recommendation system

- A Python service (separate process, `recommendation.python.base-url`) runs KNN and hybrid models.
- `RecommendationScheduler` triggers a nightly rebuild at 03:00 UTC (`recommendation.rebuild.cron`).
- `UserMovieScore` captures weighted interaction scores; `MovieSimilarity` stores pre-computed item-KNN similarity.

### Custom ID generation

All entities use a custom `IdGenerator` (strategy: `com.movie.api.storage.id.IdGenerator`) instead of auto-increment. IDs are `Long`.

### API response envelope

All responses use `ApiMessageDto<T>` with fields: `result` (boolean), `data`, `message`, `code`. Lists use `ResponseListDto<T>` which adds `totalElements` and `totalPages`.

### Validation pattern

Input forms live in `form/` and use custom constraint annotations from `validation/` (e.g. `@MovieTypeConstraint`, `@AgeRatingConstraint`). Validation errors are returned as `List<ErrorForm>` with field name and message.

### Swagger

Available at `/swagger-ui.html` when running locally.
