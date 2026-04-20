# Repository Guidelines

## Project Structure & Module Organization
This repository is organized around the backend service in `source/`. Application code lives in `source/src/main/java/com/movie/api`, grouped by role: `controller`, `service`, `storage`, `dto`, `form`, `mapper`, `cfg`, and `validation`. Runtime configuration and Liquibase changelogs live in `source/src/main/resources`, especially `application*.properties` and `resources/liquibase/`. Deployment assets are separated at the repo root in `docker/`, `deploy/`, `deploy-dev/`, and `dev-ops/`.

## Build, Test, and Development Commands
Run commands from `source/`.

- `./mvnw clean package` or `mvnw.cmd clean package`: compile the service and build the JAR.
- `./mvnw test`: run the test suite once tests are present.
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`: start the API with the local profile.
- `docker build . -t movie-api:local`: build a container image from the repo root.

Use Java 11 when building locally; the Maven configuration in `source/pom.xml` targets that version.

## Coding Style & Naming Conventions
Follow standard Spring Boot Java conventions with 4-space indentation and one public class per file. Keep package names lowercase under `com.movie.api`. Use `PascalCase` for classes, `camelCase` for methods and fields, and suffix types by responsibility, for example `MovieController`, `MovieService`, `MovieRepository`, `MovieDto`, `CreateMovieForm`, and `MovieCriteria`. Keep REST controllers thin; move business logic into services and persistence logic into repositories.

## Testing Guidelines
There is currently no committed `source/src/test` tree, so new work should add focused tests alongside the feature. Prefer JUnit-based Spring tests under `source/src/test/java/com/movie/api/...`, mirroring production packages. Name unit tests `*Test` and broader Spring integration tests `*IT`. Run `mvnw.cmd test` before opening a PR.

## Commit & Pull Request Guidelines
Recent history uses short, imperative commit subjects such as `config mqtt` and `refactor update mark survey`. Keep commits scoped and descriptive, ideally starting with a verb like `add`, `fix`, `refactor`, or `config`. Pull requests should include a concise summary, linked issue or task, configuration or schema impacts, and sample request/response output when API behavior changes.

## Configuration & Data Notes
Do not commit real secrets in `application*.properties`. Document any new required environment values in `README.md` and update Liquibase changelogs in `source/src/main/resources/liquibase/` for schema changes.
