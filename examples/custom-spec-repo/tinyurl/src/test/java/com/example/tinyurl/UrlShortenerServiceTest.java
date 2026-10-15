package com.example.tinyurl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlShortenerServiceTest {

    private final InMemoryUrlStore store = new InMemoryUrlStore();
    private final ShortCodeGenerator generator = new ShortCodeGenerator();
    private final UrlShortenerService service = new UrlShortenerService(store, generator);

    @Test
    void shortenReturnsUniqueCodeAndStoresMapping() {
        String code = service.shorten("https://example.com/some/long/path");

        assertThat(code).matches("[A-Za-z0-9]{6}");
        assertThat(store.find(code)).contains("https://example.com/some/long/path");
    }

    @Test
    void shortenRejectsMalformedUrl() {
        assertThatThrownBy(() -> service.shorten("not-a-valid-url"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void shortenRejectsNonHttpScheme() {
        assertThatThrownBy(() -> service.shorten("ftp://example.com/file"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void resolveReturnsLongUrlForKnownCode() {
        String code = service.shorten("https://example.com/some/long/path");

        assertThat(service.resolve(code)).isEqualTo("https://example.com/some/long/path");
    }

    @Test
    void resolveThrowsNotFoundForUnknownCode() {
        assertThatThrownBy(() -> service.resolve("nosuch"))
                .isInstanceOf(ShortCodeNotFoundException.class);
    }

    @Test
    void shorteningSameUrlTwiceProducesDifferentCodes() {
        String code1 = service.shorten("https://example.com/some/long/path");
        String code2 = service.shorten("https://example.com/some/long/path");

        assertThat(code1).isNotEqualTo(code2);
    }
}
