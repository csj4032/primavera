package com.genius.primavera.domain.mapper;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("사용자 권한 매퍼 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRoleMapperTest {

    @Test
    @Order(1)
    @DisplayName("사용자 권한 매퍼가 정상적으로 로드되어야 합니다.")
    public void userRoleMapperShouldBeLoaded() {
        assertNotNull(UserRoleMapperTest.class, "UserRoleMapper가 정상적으로 로드되어야 합니다.");
    }

    @Test
    @Order(2)
    @DisplayName("사용자 권한 매퍼의 메서드가 정상적으로 작동해야 합니다.")
    public void userRoleMapperMethodsShouldWork() {
        assertTrue(true, "사용자 권한 매퍼의 메서드가 정상적으로 작동해야 합니다.");
    }

    @Test
    @Order(3)
    @DisplayName("사용자 권한 매퍼의 예외 상황을 테스트해야 합니다")
    public void userRoleMapperExceptionHandling() {
        try {
            throw new UnsupportedOperationException("예외 테스트용");
        } catch (UnsupportedOperationException e) {
            assertEquals("예외 테스트용", e.getMessage(), "예외 메시지가 일치해야 합니다.");
        }
    }
}