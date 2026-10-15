package com.example.todomvc;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerDocsTest {

	@LocalServerPort
	int port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	@Test
	void givenAppRunning_whenGetApiDocs_thenReturns200() {
		// Given / When / Then
		given().when().get("/v3/api-docs").then().statusCode(200);
	}

	@Test
	void givenAppRunning_whenGetSwaggerUi_thenPageLoads() {
		// Given / When / Then
		given().redirects().follow(true).when().get("/swagger-ui.html").then().statusCode(200);
	}
}
