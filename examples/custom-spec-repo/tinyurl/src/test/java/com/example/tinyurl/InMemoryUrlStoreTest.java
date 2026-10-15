package com.example.tinyurl;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryUrlStoreTest {

    @Test
    void savesAndRetrievesUrlByCode() {
        InMemoryUrlStore store = new InMemoryUrlStore();

        store.save("abc123", "https://example.com");

        assertThat(store.find("abc123")).contains("https://example.com");
    }

    @Test
    void findReturnsEmptyForUnknownCode() {
        InMemoryUrlStore store = new InMemoryUrlStore();

        Optional<String> result = store.find("nope00");

        assertThat(result).isEmpty();
    }

    @Test
    void containsReflectsWhetherCodeIsTaken() {
        InMemoryUrlStore store = new InMemoryUrlStore();

        assertThat(store.contains("abc123")).isFalse();

        store.save("abc123", "https://example.com");

        assertThat(store.contains("abc123")).isTrue();
    }
}
