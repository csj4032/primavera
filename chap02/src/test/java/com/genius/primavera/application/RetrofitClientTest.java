package com.genius.primavera.application;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("RetrofitClient Test")
public class RetrofitClientTest {

    @Test
    @DisplayName("RetrofitClient should not be null")
    public void testRetrofitClientNotNull() {
        Retrofit retrofitClient = RetrofitClient.getInstance();
        assertNotNull(retrofitClient, "RetrofitClient should not be null");
        log.info("RetrofitClient is not null");
    }

    @Test
    @DisplayName("RetrofitClient should have correct base URL")
    public void testRetrofitClientBaseUrl() {
        Retrofit retrofitClient = RetrofitClient.getInstance();
        String baseUrl = retrofitClient.baseUrl().toString();
        assertEquals("https://www.plantplaces.com/", baseUrl, "RetrofitClient should have the correct base URL");
        log.info("RetrofitClient base URL is correct: {}", baseUrl);
    }

    @Test
    @DisplayName("RetrofitClient should be a singleton")
    public void testRetrofitClientSingleton() {
        Retrofit firstInstance = RetrofitClient.getInstance();
        Retrofit secondInstance = RetrofitClient.getInstance();
        assertSame(firstInstance, secondInstance, "RetrofitClient should be a singleton instance");
        log.info("RetrofitClient is a singleton instance");
    }
}