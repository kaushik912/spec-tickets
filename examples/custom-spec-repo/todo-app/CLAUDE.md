# todo-app

Todo CRUD API. Part of `spec-driven/custom-spec-repo` (see `../CLAUDE.md`). Endpoints in `USAGE.md`.

## Spec
- `../.spec/todo-app/{spec,plan,tasks}.md`.
- Spring Boot 4 test gotchas: `../docs/spring-boot-4-notes.md`.

## Stack
- Spring Boot 4.1.0, Java 17, Maven wrapper (`./mvnw`).
- data-jpa, validation, SQLite (sqlite-jdbc 3.46.1.3 + hibernate-community-dialects), springdoc 2.8.5. Test dep: spring-boot-starter-restclient (`TestRestTemplate`).

## Commands
- Run: `./mvnw spring-boot:run` (port 8080)
- Test: `./mvnw test`
- Swagger UI `/swagger-ui.html`, spec `/v3/api-docs`

## Config
- DB file `todo.db` in cwd (`jdbc:sqlite:todo.db`), `ddl-auto=update`. No env vars, no Docker.
- Tests: `src/test/resources/application-test.properties` (`test-todo.db`, `create-drop`).

## Layout
`com.example.todoapp`: `TodoController`, `TodoRepository`, `Todo`, `GlobalExceptionHandler`. Tests: `TodoControllerTest` (MockMvc), `TodoErrorBodyTest` (RANDOM_PORT, real error bodies), `SwaggerTest`. Scenarios: `docs/scenarios.md`.

## Gotchas
- SQLite file path is relative to working dir; run from project root.
