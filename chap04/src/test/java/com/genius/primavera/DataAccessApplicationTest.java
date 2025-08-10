package com.genius.primavera;

import com.genius.primavera.testcontainers.EnableTestContainers;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers
@DisplayName("DataAccessApplication Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessApplicationTest {

    @Test
    @Order(1)
    @DisplayName("Application Context Load Test")
    public void contextLoadsTest() {

    }
}