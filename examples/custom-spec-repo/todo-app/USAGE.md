# todo-app

Simple Todo CRUD API. Spring Boot 4.1.0, Java 17, SQLite.

## Run

```
./mvnw spring-boot:run
```

App listens on `:8080`. Data persists to `todo.db` in the working dir.

## Test

```
./mvnw test
```

## Swagger

- UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: http://localhost:8080/v3/api-docs

## Endpoints

| Method | Path         | Body                        | Response          |
|--------|--------------|------------------------------|--------------------|
| POST   | /todos       | `{"title": "..."}`           | 201 + Todo, or 400 |
| GET    | /todos       | —                             | 200 + Todo[]       |
| GET    | /todos/{id}  | —                             | 200 + Todo, or 404 |
| PUT    | /todos/{id}  | `{"title": "...", "completed": bool}` | 200 + Todo, or 404 |
| DELETE | /todos/{id}  | —                             | 204, or 404        |

Todo: `{id, title, completed}`

### DB Check
sqlite3 -header -column todo.db "SELECT * FROM todo;"