package com.genius.primavera.infrastructure.securty;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PasswordEncoderTest {

    private static PasswordEncoder encoder;
    private static String rawPassword = "csj4032@gmail.com";
    private static String bcrype = "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.";
    private static String noop = "{noop}password";

    @BeforeAll
    public static void setUp() {
        encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Test
    @Order(1)
    @DisplayName("bcrype 방식")
    public void bcrypeEncoder() {
        String encodedPassword = encoder.encode(rawPassword);
        Assertions.assertNotEquals(bcrype, encodedPassword);
        Assertions.assertTrue(encoder.matches(rawPassword, encodedPassword));
        Assertions.assertTrue(encoder.matches(rawPassword, bcrype));
    }

    @Test
    @Order(2)
    @DisplayName("noop 방식")
    public void noopEncoder() {
        Assertions.assertTrue(encoder.matches(rawPassword, noop));
        Assertions.assertTrue(encoder.matches("csj4032@gmail.com", "{bcrypt}$2a$10$XjQRpeo6I0YEfjjOxLEas.DC4knVsy5ceaz1QtrX1zsagGcmr9uF2"));
    }
}