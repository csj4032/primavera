package com.genius.primavera.interfaces;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PrimaveraResponseAdvice Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraResponseAdviceTest {


    @Test
    @Order(1)
    @DisplayName("Response Body Logging Test")
    public void responseBodyLoggingTest() {
        PrimaveraResponseAdvice advice = new PrimaveraResponseAdvice();
        Object responseBody = "Test Response";
        Object result = advice.beforeBodyWrite(responseBody, null, null, null, null, null);
        assertEquals(responseBody, result);
        assertDoesNotThrow(() -> advice.beforeBodyWrite(responseBody, null, null, null, null, null));
    }

    @Test
    @Order(2)
    @DisplayName("Supports Method Test")
    public void supportsMethodTest() {
        PrimaveraResponseAdvice advice = new PrimaveraResponseAdvice();
        boolean supports = advice.supports(null, null);
        assertTrue(supports, "Supports method should return true for all cases");
    }

    @Test
    @Order(3)
    @DisplayName("Logging Test")
    public void loggingTest() {
        PrimaveraResponseAdvice advice = new PrimaveraResponseAdvice();
        Object responseBody = "Logging Test";
        Object result = advice.beforeBodyWrite(responseBody, null, null, null, null, null);
        assertEquals(responseBody, result);
        assertDoesNotThrow(() -> advice.beforeBodyWrite(responseBody, null, null, null, null, null));
    }

    @Test
    @Order(4)
    @DisplayName("Null Response Body Test")
    public void nullResponseBodyTest() {
        PrimaveraResponseAdvice advice = new PrimaveraResponseAdvice();
        Object result = advice.beforeBodyWrite(null, null, null, null, null, null);
        assertNull(result, "Null response body should return null");
        assertDoesNotThrow(() -> advice.beforeBodyWrite(null, null, null, null, null, null));
    }

    @Test
    @Order(5)
    @DisplayName("Empty Response Body Test")
    public void emptyResponseBodyTest() {
        PrimaveraResponseAdvice advice = new PrimaveraResponseAdvice();
        Object responseBody = "";
        Object result = advice.beforeBodyWrite(responseBody, null, null, null, null, null);
        assertEquals(responseBody, result, "Empty response body should return empty string");
        assertDoesNotThrow(() -> advice.beforeBodyWrite(responseBody, null, null, null, null, null));
    }

    @Test
    @Order(6)
    @DisplayName("Response Body with Special Characters Test")
    public void responseBodyWithSpecialCharactersTest() {
        PrimaveraResponseAdvice advice = new PrimaveraResponseAdvice();
        String responseBody = "Test Response with Special Characters: !@#$%^&*()_+";
        Object result = advice.beforeBodyWrite(responseBody, null, null, null, null, null);
        assertEquals(responseBody, result, "Response body with special characters should be handled correctly");
        assertDoesNotThrow(() -> advice.beforeBodyWrite(responseBody, null, null, null, null, null));
    }
}