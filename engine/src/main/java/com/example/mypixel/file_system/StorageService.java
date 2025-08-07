package com.example.mypixel.file_system;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    Path getRootLocation();

    void createFolder(String name);

    void store(MultipartFile file);

    void store(MultipartFile file, String filename);

    void store(Resource file, String filename);

    void store(InputStream inputStream, String filename);

    void store(String source, String target);

    Stream<Path> loadAll(String relativePath);

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

    void delete(String path);
}