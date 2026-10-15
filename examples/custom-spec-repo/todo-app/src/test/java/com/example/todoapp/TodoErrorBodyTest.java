package com.example.todoapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// Real embedded server: exercises Spring's /error dispatch, which MockMvc skips.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class TodoErrorBodyTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void cleanDatabase() {
        todoRepository.deleteAll();
    }

    // SCN-004
    @Test
    void updateWithMalformedJsonReturnsEmptyBadRequestOverRealHttp() {
        // Given
        Todo saved = todoRepository.save(new Todo("Buy milk"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/todos/" + saved.getId(), HttpMethod.PUT,
                new HttpEntity<>("{not json", headers), String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNullOrEmpty();
    }

    // SCN-005
    @Test
    void updateWithNonNumericIdReturnsEmptyBadRequestOverRealHttp() {
        // Given: no todos needed
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/todos/abc", HttpMethod.PUT,
                new HttpEntity<>("{\"title\":\"x\",\"completed\":false}", headers), String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNullOrEmpty();
    }
}
