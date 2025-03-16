package com.example.mypixel.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

import com.example.mypixel.config.StorageProperties;
import com.example.mypixel.exception.StorageException;
import com.example.mypixel.exception.StorageFileNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import static com.google.common.io.Files.getFileExtension;



@Slf4j
public class TempStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public TempStorageService(@NonNull StorageProperties properties) {

        if (properties.getLocation().trim().length() == 0) {
            throw new StorageException("File upload location can not be Empty.");
        }

        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new StorageException("Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public void store(Resource file, String filename) {
        log.debug("Attempting to store file with filename: {}", filename);

        try {
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(filename))
                    .normalize().toAbsolutePath();

            log.debug("Destination path resolved to: {}", destinationFile);

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                log.error("Security violation: Attempted to store file outside of root location. Path: {}", destinationFile);
                throw new StorageException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
                log.info("Successfully stored file: {} to location: {}", filename, destinationFile);
            }
        } catch (IOException e) {
            log.error("Failed to store file: {}. Error: {}", filename, e.getMessage(), e);
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("MalformedURLException: Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public String createTempFileFromResource(Resource resource) {
        if (resource != null) {
            String filename = resource.getFilename();
            String extension = getFileExtension(filename);
            String tempName = UUID.randomUUID() + "." + extension;
            store(resource, tempName);

            log.info("Temp file created: [{}], Filename: [{}], Extension: [{}]", tempName, filename, extension);
            return tempName;
        }
        throw new StorageException("Failed to create temp file: Input resource is null");
    }

    @Override
    public String createTempFileFromFilename(String filename) {
        Resource resource = loadAsResource(filename);
        return createTempFileFromResource(resource);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
