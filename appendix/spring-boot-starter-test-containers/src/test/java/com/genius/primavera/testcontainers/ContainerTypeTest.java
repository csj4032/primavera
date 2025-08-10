package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("ContainerType Unit Tests")
class ContainerTypeTest {

    @Test
    @DisplayName("MariaDB type has correct default image")
    void testMariaDbDefaultImage() {
        assertEquals("mariadb:11.4.7", ContainerType.MARIADB.getDefaultImage(),
                "MariaDB should have correct default image");

        log.info(" MariaDB default image: {}", ContainerType.MARIADB.getDefaultImage());
    }

    @Test
    @DisplayName("Redis type has correct default image")
    void testRedisDefaultImage() {
        assertEquals("redis:7-alpine", ContainerType.REDIS.getDefaultImage(), "Redis should have correct default image");
        log.info(" Redis default image: {}", ContainerType.REDIS.getDefaultImage());
    }

    @Test
    @DisplayName("Kafka type has correct default image")
    void testKafkaDefaultImage() {
        assertEquals("confluentinc/cp-kafka:7.5.0", ContainerType.KAFKA.getDefaultImage(),
                "Kafka should have correct default image");

        log.info(" Kafka default image: {}", ContainerType.KAFKA.getDefaultImage());
    }

    @Test
    @DisplayName("MongoDB type has correct default image")
    void testMongoDbDefaultImage() {
        assertEquals("mongo:7", ContainerType.MONGODB.getDefaultImage(),
                "MongoDB should have correct default image");

        log.info(" MongoDB default image: {}", ContainerType.MONGODB.getDefaultImage());
    }

    @Test
    @DisplayName("PostgreSQL type has correct default image")
    void testPostgreSqlDefaultImage() {
        assertEquals("postgres:16", ContainerType.POSTGRESQL.getDefaultImage(),
                "PostgreSQL should have correct default image");

        log.info(" PostgreSQL default image: {}", ContainerType.POSTGRESQL.getDefaultImage());
    }

    @Test
    @DisplayName("MySQL type has correct default image")
    void testMySqlDefaultImage() {
        assertEquals("mysql:8.0", ContainerType.MYSQL.getDefaultImage(), "MySQL should have correct default image");
        log.info(" MySQL default image: {}", ContainerType.MYSQL.getDefaultImage());
    }

    @Test
    @DisplayName("Elasticsearch type has correct default image")
    void testElasticsearchDefaultImage() {
        assertEquals("docker.elastic.co/elasticsearch/elasticsearch:8.13.4", ContainerType.ELASTICSEARCH.getDefaultImage(), "Elasticsearch should have correct default image");
        log.info(" Elasticsearch default image: {}", ContainerType.ELASTICSEARCH.getDefaultImage());
    }

    @Test
    @DisplayName("All container types are defined")
    void testAllContainerTypesDefined() {
        ContainerType[] types = ContainerType.values();
        assertTrue(types.length >= 7, "Should have at least 7 container types defined");

        for (ContainerType type : types) {
            assertNotNull(type.getDefaultImage(), "Container type " + type + " should have default image");
            assertFalse(type.getDefaultImage().isEmpty(), "Container type " + type + " default image should not be empty");
        }

        log.info(" All {} container types have default images", types.length);
    }

    @Test
    @DisplayName("Container type names are consistent")
    void testContainerTypeNames() {
        ContainerType[] types = ContainerType.values();

        for (ContainerType type : types) {
            String name = type.name();
            assertNotNull(name, "Container type name should not be null");
            assertFalse(name.isEmpty(), "Container type name should not be empty");
            assertTrue(name.equals(name.toUpperCase()),
                    "Container type name should be uppercase: " + name);
        }

        log.info(" All container type names are properly formatted");
    }

    @Test
    @DisplayName("Container type valueOf works correctly")
    void testValueOf() {
        assertEquals(ContainerType.MARIADB, ContainerType.valueOf("MARIADB"),
                "valueOf should work for MARIADB");
        assertEquals(ContainerType.REDIS, ContainerType.valueOf("REDIS"),
                "valueOf should work for REDIS");
        assertEquals(ContainerType.KAFKA, ContainerType.valueOf("KAFKA"),
                "valueOf should work for KAFKA");

        assertThrows(IllegalArgumentException.class, () -> {
            ContainerType.valueOf("NONEXISTENT");
        }, "valueOf should throw exception for non-existent type");

        log.info(" valueOf method works correctly");
    }

    @Test
    @DisplayName("Container type ordinal values are stable")
    void testOrdinalValues() {
        ContainerType[] types = ContainerType.values();

        for (int i = 0; i < types.length; i++) {
            assertEquals(i, types[i].ordinal(),
                    "Ordinal value should match array index for " + types[i]);
        }

        log.info(" Ordinal values are stable and consistent");
    }
}