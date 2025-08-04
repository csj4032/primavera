package com.genius.primavera.application;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import retrofit2.Retrofit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RetrofitClientConfig Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RetrofitClientConfigTest {

    @Autowired
    private Retrofit retrofit;

    @Test
    @Order(1)
    @DisplayName("RetrofitClientConfig should not be null")
    public void testRetrofitClientConfigNotNull() {
        RetrofitClientConfig retrofitClientConfig = new RetrofitClientConfig();
        assertNotNull(retrofitClientConfig, "RetrofitClientConfig should not be null");
    }

    @Test
    @Order(2)
    @DisplayName("RetrofitClientConfig should create a valid Retrofit instance")
    public void testCreateRetrofitInstance() {
        assertNotNull(retrofit, "Retrofit bean should not be null");
        assertNotNull(retrofit.baseUrl(), "Base URL should not be null");
        log.info("Retrofit base URL: {}", retrofit.baseUrl());
        assertEquals("https://api.plantplaces.com/", retrofit.baseUrl().toString());
    }
}