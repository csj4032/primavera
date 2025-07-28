package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Slf4j
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
public class NoAnnotationTest {

    @Test
    public void testWithoutEnablePrimaveraTestcontainers() {
        log.info("This test should run without starting any TestContainers");
        // 이 테스트는 @EnablePrimaveraTestcontainers 애노테이션이 없으므로
        // MariaDB 컨테이너가 시작되지 않아야 함
    }
}