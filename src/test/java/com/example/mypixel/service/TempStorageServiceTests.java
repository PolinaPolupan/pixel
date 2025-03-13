package com.example.mypixel.service;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.example.mypixel.config.StorageProperties;
import com.example.mypixel.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TempStorageServiceTests {

    private final StorageProperties properties = new StorageProperties();
    private TempStorageService service;

    @TempDir
    File tempDir;

    @BeforeEach
    public void init() {
        properties.setLocation(tempDir.getAbsolutePath());
        service = new TempStorageService(properties);
        service.init();
    }

    @Test
    public void emptyUploadLocation() {
        service = null;
        properties.setLocation("");
        assertThrows(StorageException.class, () -> service = new TempStorageService(properties));
    }

    @Test
    public void loadNonExistent() {
        assertThat(service.load("foo.jpg")).doesNotExist();
    }

    @Test
    public void saveAndLoad() {
        service.store(new MockMultipartFile("foo", "foo.jpg", MediaType.IMAGE_JPEG_VALUE,
                "Hello, World".getBytes()));
        assertThat(service.load("foo.jpg")).exists();
    }

    @Test
    public void saveRelativePathNotPermitted() {
        assertThrows(StorageException.class, () -> service.store(new MockMultipartFile("foo", "../foo.jpg",
                MediaType.IMAGE_JPEG_VALUE, "Hello, World".getBytes())));
    }

    @Test
    public void saveAbsolutePathNotPermitted() {
        assertThrows(StorageException.class, () -> service.store(new MockMultipartFile("foo", "/etc/passwd",
                MediaType.IMAGE_JPEG_VALUE, "Hello, World".getBytes())));
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    public void saveAbsolutePathInFilenamePermitted() {
        //Unix file systems (e.g. ext4) allows backslash '\' in file names.
        String fileName="\\etc\\passwd";
        service.store(new MockMultipartFile(fileName, fileName,
                MediaType.IMAGE_JPEG_VALUE, "Hello, World".getBytes()));
        assertTrue(Files.exists(
                Paths.get(properties.getLocation()).resolve(Paths.get(fileName))));
    }

    @Test
    public void savePermitted() {
        service.store(new MockMultipartFile("foo", "bar/../foo.jpg",
                MediaType.IMAGE_JPEG_VALUE, "Hello, World".getBytes()));
    }


}
