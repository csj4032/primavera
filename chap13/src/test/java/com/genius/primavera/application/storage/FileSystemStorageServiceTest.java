package com.genius.primavera.application.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileSystemStorageServiceTest {

    @Autowired
    private StorageProperties properties;

    @Autowired
    private FileSystemStorageService fileSystemStorageService;

    @Test
    @Order(1)
    @DisplayName("connection test verification")
    public void propertiesTest() {
        Assertions.assertEquals("upload", properties.getLocation());
    }

    @Test
    @Order(2)
    @DisplayName("connection test creation")
    public void initTest() {
        Assertions.assertTrue(new File(properties.getLocation()).exists());
    }

    @Test
    @Order(3)
    @DisplayName("test")
    public void storeTest() {
        MockMultipartFile file = new MockMultipartFile("genius", "genius.txt", "txt", "Hello World".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("genius2", "genius2.txt", "txt", "Hello World".getBytes());
        fileSystemStorageService.store(file);
        fileSystemStorageService.store(file2);
    }

    @Test
    @Order(4)
    @DisplayName("test file")
    public void loadTest() {
        Path path = fileSystemStorageService.load("genius.txt");
        Assertions.assertEquals("genius.txt", path.toFile().getName());
    }

    @Test
    @Order(5)
    @DisplayName("test file")
    public void loadAllTest() {
        Stream<Path> paths = fileSystemStorageService.loadAll();
        Assertions.assertEquals(2, paths.count());
    }

    @Test
    @Order(6)
    @DisplayName("connection test deletion")
    public void deleteAllTest() {
        fileSystemStorageService.deleteAll();
        Assertions.assertFalse(new File(properties.getLocation()).exists());
    }
}