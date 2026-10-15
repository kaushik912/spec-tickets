# Spring Boot 4.1 gotchas

Notes from debugging `tinyurl`'s test setup on `spring-boot-starter-parent:4.1.0`. Boot 4 split test infra into per-web-stack modules, so paths/deps that worked on Boot 3 silently don't resolve.

## `@WebMvcTest` moved
Package is `org.springframework.boot.webmvc.test.autoconfigure`, not `org.springframework.boot.test.autoconfigure.web.servlet` (Boot 3 location).

## `@WebMvcTest` excludes `@Service`/`@Component` beans
It only loads the web layer. Mock collaborators with `@MockitoBean` (the `@MockBean` replacement) rather than expecting real service beans to be wired.

## `TestRestTemplate` moved and isn't auto-wired
Now `org.springframework.boot.resttestclient.TestRestTemplate`. Getting a bean requires the explicit `@AutoConfigureTestRestTemplate` annotation on the test class — Boot 3's implicit wiring under `@SpringBootTest(webEnvironment = RANDOM_PORT)` is gone.

## `TestRestTemplate` needs an extra dependency
`RestTemplateBuilder` lives in `spring-boot-starter-restclient`, not pulled in by `spring-boot-starter-webmvc-test`. Without it you get `NoClassDefFoundError: org/springframework/boot/restclient/RestTemplateBuilder` at context startup, surfaced as an opaque `@ConditionalOnMissingBean` failure.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-restclient</artifactId>
    <scope>test</scope>
</dependency>
```

## `TestRestTemplate` follows redirects by default
Asserting a 3xx response (e.g. a redirect endpoint) requires disabling redirect-following on the client, or the test silently follows the `Location` header and asserts against *that* response instead:

```java
restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory() {
    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        super.prepareConnection(connection, httpMethod);
        connection.setInstanceFollowRedirects(false);
    }
});
```

## Missing `@Component`/`@Service` only surfaces under a full context
Slice tests (`@WebMvcTest`, `@DataJpaTest`, etc.) mock or skip unrelated beans, so a class missing its stereotype annotation still passes them. Only a full `@SpringBootTest` (or running the app) exercises real bean wiring and catches it.
