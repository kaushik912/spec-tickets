package com.example.tinyurl;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UrlShortenerService {

    private final InMemoryUrlStore store;
    private final ShortCodeGenerator generator;

    public UrlShortenerService(InMemoryUrlStore store, ShortCodeGenerator generator) {
        this.store = store;
        this.generator = generator;
    }

    public String shorten(String url) {
        validate(url);

        String code;
        do {
            code = generator.generate();
        } while (store.contains(code));

        store.save(code, url);
        return code;
    }

    public String resolve(String code) {
        return store.find(code)
                .orElseThrow(() -> new ShortCodeNotFoundException("Unknown short code: " + code));
    }

    private void validate(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Malformed URL: " + url);
        }

        String scheme = uri.getScheme();
        if ((!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || uri.getHost() == null || uri.getHost().isEmpty()) {
            throw new InvalidUrlException("URL must be an absolute http/https URL: " + url);
        }
    }
}
