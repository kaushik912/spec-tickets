package com.example.tinyurl;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUrlStore {

    private final Map<String, String> codeToUrl = new ConcurrentHashMap<>();

    public void save(String code, String url) {
        codeToUrl.put(code, url);
    }

    public Optional<String> find(String code) {
        return Optional.ofNullable(codeToUrl.get(code));
    }

    public boolean contains(String code) {
        return codeToUrl.containsKey(code);
    }
}
