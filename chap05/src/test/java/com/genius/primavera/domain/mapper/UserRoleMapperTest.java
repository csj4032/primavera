package com.genius.primavera.domain.mapper;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("user translated_text_2 translated_text_2 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRoleMapperTest {

    @Test
    @Order(1)
    @DisplayName("user translated_text_2 translated_text_2translated_text_1 successfully translated_text_5 translated_text_3.")
    public void userRoleMapperShouldBeLoaded() {
        assertNotNull(UserRoleMapperTest.class, "UserRoleMappertranslated_text_1 successfully translated_text_5 translated_text_3.");
    }

    @Test
    @Order(2)
    @DisplayName("user translated_text_2 translated_text_2 translated_text_1 successfully translated_text_4 translated_text_3.")
    public void userRoleMapperMethodsShouldWork() {
        assertTrue(true, "user translated_text_2 translated_text_2 translated_text_1 successfully translated_text_4 translated_text_3.");
    }

    @Test
    @Order(3)
    @DisplayName("user translated_text_2 translated_text_2 exception translated_text_3 test translated_text_3")
    public void userRoleMapperExceptionHandling() {
        try {
            throw new UnsupportedOperationException("exception test");
        } catch (UnsupportedOperationException e) {
            assertEquals("exception test", e.getMessage(), "exception translated_text_1 translated_text_4 translated_text_3.");
        }
    }
}