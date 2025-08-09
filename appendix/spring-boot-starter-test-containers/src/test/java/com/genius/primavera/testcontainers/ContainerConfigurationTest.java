package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("ContainerConfiguration Unit Tests")
class ContainerConfigurationTest {

    @Test
    @DisplayName("ContainerSpec builder works correctly")
    void testContainerSpecBuilder() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
                .image("test:latest")
                .database("test_db")
                .username("test_user")
                .password("test_pass")
                .initScript("init.sql")
                .startupTimeout(120)
                .environment(Map.of("ENV_VAR", "value"))
                .networkAliases(new String[]{"alias1", "alias2"})
                .build();

        assertEquals("test:latest", spec.getImage(), "Image should be set");
        assertEquals("test_db", spec.getDatabase(), "Database should be set");
        assertEquals("test_user", spec.getUsername(), "Username should be set");
        assertEquals("test_pass", spec.getPassword(), "Password should be set");
        assertEquals("init.sql", spec.getInitScript(), "Init script should be set");
        assertEquals(120, spec.getStartupTimeout(), "Startup timeout should be set");
        assertNotNull(spec.getEnvironment(), "Environment should not be null");
        assertEquals("value", spec.getEnvironment().get("ENV_VAR"), "Environment variable should be set");
        assertNotNull(spec.getNetworkAliases(), "Network aliases should not be null");
        assertEquals(2, spec.getNetworkAliases().length, "Should have 2 network aliases");
        assertEquals("alias1", spec.getNetworkAliases()[0], "First alias should be correct");

        log.info("✅ ContainerSpec builder works correctly");
    }

    @Test
    @DisplayName("ContainerSpec getImageOrDefault works")
    void testGetImageOrDefault() {
        ContainerConfiguration.ContainerSpec specWithImage = ContainerConfiguration.ContainerSpec.builder()
                .image("custom:1.0")
                .build();

        ContainerConfiguration.ContainerSpec specWithoutImage = ContainerConfiguration.ContainerSpec.builder()
                .build();

        assertEquals("custom:1.0", specWithImage.getImageOrDefault(ContainerType.MARIADB),
                "Should return custom image when set");

        assertEquals("mariadb:11.4.7", specWithoutImage.getImageOrDefault(ContainerType.MARIADB),
                "Should return default image when not set");

        log.info("✅ getImageOrDefault works correctly");
    }

    @Test
    @DisplayName("ContainerSpec getUsernameOrDefault works")
    void testGetUsernameOrDefault() {
        ContainerConfiguration.ContainerSpec specWithUsername = ContainerConfiguration.ContainerSpec.builder()
                .username("custom_user")
                .build();

        ContainerConfiguration.ContainerSpec specWithoutUsername = ContainerConfiguration.ContainerSpec.builder()
                .build();

        assertEquals("custom_user", specWithUsername.getUsernameOrDefault(),
                "Should return custom username when set");

        assertEquals("primavera", specWithoutUsername.getUsernameOrDefault(),
                "Should return default username when not set");

        log.info("✅ getUsernameOrDefault works correctly");
    }

    @Test
    @DisplayName("ContainerSpec getPasswordOrDefault works")
    void testGetPasswordOrDefault() {
        ContainerConfiguration.ContainerSpec specWithPassword = ContainerConfiguration.ContainerSpec.builder()
                .password("custom_pass")
                .build();

        ContainerConfiguration.ContainerSpec specWithoutPassword = ContainerConfiguration.ContainerSpec.builder()
                .build();

        assertEquals("custom_pass", specWithPassword.getPasswordOrDefault(),
                "Should return custom password when set");

        assertEquals("primavera", specWithoutPassword.getPasswordOrDefault(),
                "Should return default password when not set");

        log.info("✅ getPasswordOrDefault works correctly");
    }

    @Test
    @DisplayName("ContainerSpec getDatabaseOrDefault works")
    void testGetDatabaseOrDefault() {
        ContainerConfiguration.ContainerSpec specWithDatabase = ContainerConfiguration.ContainerSpec.builder()
                .database("custom_db")
                .build();

        ContainerConfiguration.ContainerSpec specWithoutDatabase = ContainerConfiguration.ContainerSpec.builder()
                .build();

        assertEquals("custom_db", specWithDatabase.getDatabaseOrDefault(),
                "Should return custom database when set");

        assertEquals("primavera", specWithoutDatabase.getDatabaseOrDefault(),
                "Should return default database when not set");

        log.info("✅ getDatabaseOrDefault works correctly");
    }

    @Test
    @DisplayName("ContainerSpec getStartupTimeoutOrDefault works")
    void testGetStartupTimeoutOrDefault() {
        ContainerConfiguration.ContainerSpec specWithTimeout = ContainerConfiguration.ContainerSpec.builder()
                .startupTimeout(180)
                .build();

        ContainerConfiguration.ContainerSpec specWithoutTimeout = ContainerConfiguration.ContainerSpec.builder()
                .build();

        assertEquals(180, specWithTimeout.getStartupTimeoutOrDefault(),
                "Should return custom timeout when set");

        assertEquals(60, specWithoutTimeout.getStartupTimeoutOrDefault(),
                "Should return default timeout when not set");

        log.info("✅ getStartupTimeoutOrDefault works correctly");
    }

    @Test
    @DisplayName("ContainerConfiguration can hold multiple container specs")
    void testMultipleContainerSpecs() {
        ContainerConfiguration config = new ContainerConfiguration();

        ContainerConfiguration.ContainerSpec spec1 = ContainerConfiguration.ContainerSpec.builder()
                .image("mariadb:11.4.7")
                .database("db1")
                .build();

        ContainerConfiguration.ContainerSpec spec2 = ContainerConfiguration.ContainerSpec.builder()
                .image("redis:7-alpine")
                .password("redis_pass")
                .build();

        Map<String, ContainerConfiguration.ContainerSpec> containers = Map.of(
                "database", spec1,
                "cache", spec2
        );

        config.setContainers(containers);

        assertNotNull(config.getContainers(), "Containers map should not be null");
        assertEquals(2, config.getContainers().size(), "Should have 2 container specs");

        ContainerConfiguration.ContainerSpec retrievedSpec1 = config.getContainers().get("database");
        ContainerConfiguration.ContainerSpec retrievedSpec2 = config.getContainers().get("cache");

        assertNotNull(retrievedSpec1, "Database spec should be retrievable");
        assertNotNull(retrievedSpec2, "Cache spec should be retrievable");

        assertEquals("db1", retrievedSpec1.getDatabase(), "Database name should be correct");
        assertEquals("redis_pass", retrievedSpec2.getPassword(), "Cache password should be correct");

        log.info("✅ Multiple container specs work correctly");
    }

    @Test
    @DisplayName("ContainerSpec no-args constructor works")
    void testNoArgsConstructor() {
        ContainerConfiguration.ContainerSpec spec = new ContainerConfiguration.ContainerSpec();

        assertNull(spec.getImage(), "Image should be null by default");
        assertNull(spec.getDatabase(), "Database should be null by default");
        assertNull(spec.getUsername(), "Username should be null by default");
        assertNull(spec.getPassword(), "Password should be null by default");
        assertNull(spec.getStartupTimeout(), "Startup timeout should be null by default");

        assertEquals("primavera", spec.getUsernameOrDefault(), "Should use default username");
        assertEquals("primavera", spec.getPasswordOrDefault(), "Should use default password");
        assertEquals("primavera", spec.getDatabaseOrDefault(), "Should use default database");
        assertEquals(60, spec.getStartupTimeoutOrDefault(), "Should use default timeout");

        log.info("✅ No-args constructor works correctly");
    }

    @Test
    @DisplayName("ContainerSpec all-args constructor works")
    void testAllArgsConstructor() {
        Map<String, String> env = Map.of("TEST", "value");
        String[] aliases = {"alias1", "alias2"};

        ContainerConfiguration.ContainerSpec spec = new ContainerConfiguration.ContainerSpec(
                "test:latest", "test_db", "test_user", "test_pass", "token", "init.sql", 120, env, aliases);

        assertEquals("test:latest", spec.getImage(), "Image should be set");
        assertEquals("test_db", spec.getDatabase(), "Database should be set");
        assertEquals("test_user", spec.getUsername(), "Username should be set");
        assertEquals("test_pass", spec.getPassword(), "Password should be set");
        assertEquals("init.sql", spec.getInitScript(), "Init script should be set");
        assertEquals(120, spec.getStartupTimeout(), "Startup timeout should be set");
        assertSame(env, spec.getEnvironment(), "Environment should be same instance");
        assertSame(aliases, spec.getNetworkAliases(), "Network aliases should be same instance");

        log.info("✅ All-args constructor works correctly");
    }
}