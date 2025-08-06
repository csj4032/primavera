package com.genius.primavera.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserStatus Test")
public class UserStatusTest {

    @Test
    @DisplayName("UserStatus Enum Test")
    public void userStatusEnumTest() {
        UserStatus active = UserStatus.ACTIVE;
        UserStatus inactive = UserStatus.INACTIVE;
        UserStatus dormant = UserStatus.DORMANT;
        UserStatus leave = UserStatus.LEAVE;
        assertEquals(1, active.getValue());
        assertEquals(2, inactive.getValue());
        assertEquals(3, dormant.getValue());
        assertEquals(4, leave.getValue());
        assertNotNull(active.name());
        assertNotNull(inactive.name());
        assertNotNull(dormant.name());
        assertNotNull(leave.name());
        assertTrue(active.ordinal() < inactive.ordinal());
        assertTrue(dormant.ordinal() < leave.ordinal());
    }
}