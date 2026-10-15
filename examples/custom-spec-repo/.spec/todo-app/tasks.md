---
status: approved
---
# Todo App CRUD API — Tasks

- [x] Scaffold project: `spring init` in `todo-app/` (Maven, Java 17, Spring Boot 4.1.0, groupId `com.example`, artifactId `todo-app`)
- [x] Add dependencies: `spring add web`, `spring add data-jpa`, `spring add validation`; manually add `sqlite-jdbc`, SQLite Hibernate dialect, and `springdoc-openapi-starter-webmvc-ui` to `pom.xml`
- [x] Configure `application.properties` (SQLite datasource, ddl-auto) and `application-test.properties` (test SQLite file)
- [x] Create `Todo` JPA entity (id, title `@NotBlank`, completed) with Swagger `@Schema` annotations
- [x] Create `TodoRepository extends JpaRepository<Todo, Long>`
- [x] Create `GlobalExceptionHandler` mapping `EntityNotFoundException`→404, `MethodArgumentNotValidException`→400
- [x] Write failing MockMvc test: `POST /todos` with valid title returns 201 + created todo (completed=false)
- [x] Implement `TodoController.create` to make the test pass
- [x] Write failing MockMvc test: `POST /todos` with blank title returns 400
- [x] Wire `@Valid` on create endpoint to make the test pass
- [x] Write failing MockMvc test: `GET /todos` returns 200 + list of all created todos
- [x] Implement `TodoController.getAll` to make the test pass
- [x] Write failing MockMvc test: `GET /todos/{id}` returns 200 + todo for existing id, 404 for missing id
- [x] Implement `TodoController.getById` to make the test pass
- [x] Write failing MockMvc test: `PUT /todos/{id}` updates title/completed and returns 200 + updated todo; 404 for missing id
- [x] Implement `TodoController.update` to make the test pass
- [x] Write failing MockMvc test: `DELETE /todos/{id}` returns 204 and a follow-up GET returns 404; DELETE on missing id returns 404
- [x] Implement `TodoController.delete` to make the test pass
- [x] Write failing MockMvc test: `GET /swagger-ui.html` and `GET /v3/api-docs` return 200
- [x] Verify springdoc auto-config serves Swagger UI to make the test pass
- [x] Manual check: run app, confirm `todo.db` file persists todos across a restart
