package com.example.todomvc.todo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoApiTest {

	@LocalServerPort
	int port;

	@Autowired
	TodoRepository repository;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		repository.deleteAll();
	}

	@Test
	void givenValidTitle_whenPost_thenReturns201WithTodoAndLocation() {
		// When / Then
		given().contentType(ContentType.JSON).body("{\"title\":\"Buy milk\"}")
			.when().post("/api/todos")
			.then().statusCode(201)
			.header("Location", containsString("/api/todos/"))
			.body("title", equalTo("Buy milk"))
			.body("completed", equalTo(false))
			.body("id", notNullValue())
			.body("createdAt", notNullValue());
	}

	@Test
	void givenPaddedTitle_whenPost_thenTitleTrimmed() {
		// When / Then
		given().contentType(ContentType.JSON).body("{\"title\":\"  hi  \"}")
			.when().post("/api/todos")
			.then().statusCode(201).body("title", equalTo("hi"));
	}

	@Test
	void givenBlankTitle_whenPost_thenReturns400WithErrorShape() {
		// When / Then
		given().contentType(ContentType.JSON).body("{\"title\":\"   \"}")
			.when().post("/api/todos")
			.then().statusCode(400)
			.body("status", equalTo(400))
			.body("error", equalTo("Bad Request"))
			.body("message", notNullValue());
	}

	@Test
	void givenMissingTitle_whenPost_thenReturns400() {
		// When / Then
		given().contentType(ContentType.JSON).body("{}")
			.when().post("/api/todos").then().statusCode(400);
	}

	@Test
	void givenTooLongTitle_whenPost_thenReturns400() {
		// When / Then
		given().contentType(ContentType.JSON).body("{\"title\":\"" + "x".repeat(201) + "\"}")
			.when().post("/api/todos").then().statusCode(400);
	}

	@Test
	void givenNoTodos_whenGet_thenReturnsEmptyArray() {
		// When / Then
		given().when().get("/api/todos").then().statusCode(200).body("$", empty());
	}

	@Test
	void givenTwoTodos_whenGet_thenReturnsOrderedById() {
		// Given
		repository.save(new Todo("first"));
		repository.save(new Todo("second"));

		// When / Then
		given().when().get("/api/todos")
			.then().statusCode(200)
			.body("$", hasSize(2))
			.body("title[0]", equalTo("first"))
			.body("title[1]", equalTo("second"));
	}

	@Test
	void givenTodo_whenPatchCompletedTrueThenFalse_thenReturnsUpdatedTodo() {
		// Given
		Long id = repository.save(new Todo("a")).getId();

		// When / Then
		given().contentType(ContentType.JSON).body("{\"completed\":true}")
			.when().patch("/api/todos/" + id)
			.then().statusCode(200).body("completed", equalTo(true)).body("id", equalTo(id.intValue()));
		given().contentType(ContentType.JSON).body("{\"completed\":false}")
			.when().patch("/api/todos/" + id)
			.then().statusCode(200).body("completed", equalTo(false));
	}

	@Test
	void givenUnknownId_whenPatch_thenReturns404WithErrorShape() {
		// When / Then
		given().contentType(ContentType.JSON).body("{\"completed\":true}")
			.when().patch("/api/todos/999999")
			.then().statusCode(404).body("status", equalTo(404)).body("error", equalTo("Not Found"))
			.body("message", notNullValue());
	}

	@Test
	void givenMissingCompleted_whenPatch_thenReturns400() {
		// Given
		Long id = repository.save(new Todo("a")).getId();

		// When / Then
		given().contentType(ContentType.JSON).body("{}")
			.when().patch("/api/todos/" + id).then().statusCode(400);
	}

	@Test
	void givenNonBooleanCompleted_whenPatch_thenReturns400() {
		// Given
		Long id = repository.save(new Todo("a")).getId();

		// When / Then
		given().contentType(ContentType.JSON).body("{\"completed\":\"maybe\"}")
			.when().patch("/api/todos/" + id).then().statusCode(400);
	}

	@Test
	void givenTodo_whenDelete_thenReturns204AndNoLongerListed() {
		// Given
		Long id = repository.save(new Todo("a")).getId();

		// When / Then
		given().when().delete("/api/todos/" + id).then().statusCode(204);
		given().when().get("/api/todos").then().statusCode(200).body("$", empty());
	}

	@Test
	void givenUnknownId_whenDelete_thenReturns404() {
		// When / Then
		given().when().delete("/api/todos/999999").then().statusCode(404).body("status", equalTo(404));
	}
}
