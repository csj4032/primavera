package com.genius.primavera.testcontainer.v3.example;

import com.genius.primavera.testcontainer.v3.ContainerType;
import com.genius.primavera.testcontainer.v3.EnableTestContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit Extension이 제대로 작동하는지 확인하는 간단한 테스트
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableTestContainer({
    @EnableTestContainer.TestContainer(type = ContainerType.MARIADB, name = "primaryDb")
})
class SimpleExtensionTest {
    
    @Test
    void testExtensionIsWorking() {
        log.info("Extension test is running");
        assertTrue(true, "Extension should be working");
    }
}