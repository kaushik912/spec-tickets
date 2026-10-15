package com.example.tinyurl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UrlShortenerEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void doNotFollowRedirects() {
        restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        });
    }

    @Test
    void shortenThenRedirectRoundTripsThroughRealServiceAndStore() {
        ResponseEntity<ShortenResponse> shortenResponse = restTemplate.postForEntity(
                "/api/shorten", new ShortenRequest("https://example.com/some/long/path"), ShortenResponse.class);

        assertThat(shortenResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String shortCode = shortenResponse.getBody().shortCode();
        assertThat(shortCode).matches("[A-Za-z0-9]{6}");

        ResponseEntity<Void> redirectResponse = restTemplate.getForEntity("/" + shortCode, Void.class);

        assertThat(redirectResponse.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(redirectResponse.getHeaders().getLocation())
                .hasToString("https://example.com/some/long/path");
    }

    @Test
    void unknownCodeReturns404() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity("/zzzzzz", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().error()).isNotBlank();
    }

    @Test
    void invalidUrlReturns400() {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/api/shorten", new ShortenRequest("not-a-valid-url"), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isNotBlank();
    }
}
