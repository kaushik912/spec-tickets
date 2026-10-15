package com.example.tinyurl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTest {

    @Test
    void generatesSixCharBase62Code() {
        ShortCodeGenerator generator = new ShortCodeGenerator();

        String code = generator.generate();

        assertThat(code).hasSize(6);
        assertThat(code).matches("[A-Za-z0-9]{6}");
    }
}
