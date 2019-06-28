package com.genius.primavera.application.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Path load(String filename);

    Stream<Path> loadAll();

    void deleteAll();

}