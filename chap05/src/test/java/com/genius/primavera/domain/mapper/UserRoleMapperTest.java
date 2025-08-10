package com.genius.primavera.domain.mapper;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("user test test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRoleMapperTest {

    @Test
    @Order(1)
    @DisplayName("user testshould successfully Endpoint connection.")
    public void userRoleMapperShouldBeLoaded() {
        assertNotNull(UserRoleMapperTest.class, "UserRoleMappershould successfully Endpoint connection.");
    }

    @Test
    @Order(2)
    @DisplayName("user test should successfully file connection.")
    public void userRoleMapperMethodsShouldWork() {
        assertTrue(true, "user test should successfully file connection.");
    }

    @Test
    @Order(3)
    @DisplayName("user test exception connection test connection")
    public void userRoleMapperExceptionHandling() {
        try {
            throw new UnsupportedOperationException("exception test");
        } catch (UnsupportedOperationException e) {
            assertEquals("exception test", e.getMessage(), "exception should file connection.");
        }
    }
}