# Copilot Instructions for movie-api

## Build & Run Commands

```bash
# Navigate to source directory first
cd source

# Build the application
mvn clean package

# Run the application (requires .env configuration)
java -jar target/movie-api.jar

# Run with specific profile
java -jar target/movie-api.jar -Dspring.profiles.active=local

# Generate Liquibase diff changelog
mvn liquibase:diff

# Docker build
docker build . --tag movie-api:v1.0
```

## Architecture Overview

This is a Spring Boot 2.3 REST API for a movie streaming platform with OAuth2/JWT authentication.

### Package Structure (`source/src/main/java/com/movie/api/`)

- **controller/** - REST endpoints extending `ABasicController` for standardized responses
- **service/** - Business logic; `feign/` for inter-service calls, `rabbit/` for async messaging, `redis/` for caching
- **storage/** - Data layer: `model/` (JPA entities), `repository/` (Spring Data JPA), `criteria/` (dynamic query specifications)
- **dto/** - Response DTOs; organized by domain (e.g., `movie/`, `account/`)
- **form/** - Request validation forms; organized by domain
- **mapper/** - MapStruct interfaces for entity-DTO conversion
- **validation/** - Custom constraint annotations with validators in `impl/`
- **cfg/** - Configuration classes; `secutity/` for OAuth2/JWT setup
- **exception/** - Custom exceptions with `GlobalExceptionHandler`

### Key Integrations

- **Auth Service** (`auth.internal.base.url`) - External authentication via Feign clients
- **Media Service** (`media.internal.base.url`) - File/video management
- **RabbitMQ** - Async video processing, notifications (`RabbitSender`, `RabbitMQListener`)
- **Redis** - Caching with key pattern `{entity}::{id}` (e.g., `movie::123`)

## Code Conventions

### API Response Format

All endpoints return `ApiMessageDto<T>`:
```java
@GetMapping("/get/{id}")
public ApiMessageDto<MovieDto> get(@PathVariable Long id) {
    return makeSuccessResponse(movieDto, "Get movie success");
}
```

### Entity Pattern

Entities extend `Auditable<String>` which provides: `id`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`, `status`. Tables use `db_` prefix (defined in `DatabaseConstant.PREFIX_TABLE`).

### Custom ID Generation

Uses custom ID generator strategy: `com.movie.api.storage.id.IdGenerator` (configured via `BaseConstant.APP_ID_GENERATOR_*`).

### MapStruct Mappers

Use `@BeanMapping(ignoreByDefault = true)` with explicit `@Mapping` annotations. Named methods allow different projection levels:
```java
@Named("entityToMovieDto")
MovieDto entityToMovieDto(Movie movie);  // Full projection

@Named("entityToMovieAutoCompleteDto")
MovieDto entityToMovieAutoCompleteDto(Movie movie);  // Minimal projection
```

### Criteria Specifications

Dynamic queries use `*Criteria` classes with `getSpecification()` returning JPA `Specification<T>`:
```java
Page<Movie> movies = movieRepository.findAll(criteria.getSpecification(), pageable);
```

### Custom Validation

Define constraint annotation in `validation/` with validator in `validation/impl/`:
```java
@StatusConstraint(allowNull = true)
private Integer status;
```

### Security

- Role-based access via `@PreAuthorize("hasRole('MOV_C')")` (MOV_C = Movie Create, MOV_U = Update, etc.)
- Public endpoints listed in `SecurityConstant.ENDPOINTS_BYPASS_JWT`
- JWT claims extracted via `UserServiceImpl.getAddInfoFromToken()`

### Constants

- Status values: `STATUS_ACTIVE=1`, `STATUS_PENDING=0`, `STATUS_LOCK=-1`, `STATUS_DELETE=-2`
- Movie types: `MOVIE_TYPE_SINGLE=1`, `MOVIE_TYPE_SERIES=2`
- Movie items: `MOVIE_ITEM_KIND_SEASON=1`, `MOVIE_ITEM_KIND_EPISODE=2`, `MOVIE_ITEM_KIND_TRAILER=3`

### Caching Pattern

```java
String key = redisService.buildKey("movie", id.toString());
MovieDto cached = redisService.get(key, MovieDto.class);
if (cached != null) {
    redisService.refreshTTL(key, 5 * 60);
    return cached;
}
// ... fetch from DB, then cache
redisService.put(key, movieDto, 5 * 60);
```

### Database Migrations

Liquibase changelogs in `source/src/main/resources/liquibase/`. Master file: `db.changelog-master.xml`.

## Environment Setup

Copy `source/.env.example` to `source/.env` and configure:
- Database (MySQL), Redis, RabbitMQ connections
- JWT signing key (`AUTH_SIGNING_KEY`)
- External service URLs
