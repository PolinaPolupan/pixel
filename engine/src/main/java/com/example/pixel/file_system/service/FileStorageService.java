package com.example.pixel.file_system.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import com.example.pixel.common.exception.StorageException;
import com.example.pixel.common.exception.StorageFileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileStorageService implements StorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${storage.directory}") String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new StorageException("File upload location can not be Empty.");
        }
        this.rootLocation = Paths.get(location);
    }

    @Override
    public Path getRootLocation() {
        return rootLocation;
    }

    @Override
    public synchronized void store(MultipartFile file) {
        store(file, file.getOriginalFilename());
    }

    @Override
    public synchronized void store(MultipartFile file, String filename) {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            store(inputStream, filename);
        } catch (IOException e) {
            throw new StorageException("Failed to access file content.", e);
        }
    }

    @Override
    public synchronized void store(Resource file, String filename) {
        log.debug("Storing resource as file: {}", filename);

        try (InputStream inputStream = file.getInputStream()) {
            store(inputStream, filename);
        } catch (IOException e) {
            log.error("Failed to access resource content: {}", e.getMessage(), e);
            throw new StorageException("Failed to access resource content.", e);
        }
    }

    /**
     * Stores a file from an input stream.
     * Note: This method does NOT close the input stream - the caller is responsible for that.
     */
    @Override
    public synchronized void store(InputStream inputStream, String filename) {
        log.debug("Storing file with filename: {}", filename);

        if (inputStream == null) {
            throw new StorageException("InputStream cannot be null");
        }

        if (filename == null || filename.isEmpty()) {
            throw new StorageException("Filename cannot be empty");
        }

        String path = extractPath(filename);
        this.createFolder(path);

        try {
            Path destinationFile = resolveAndValidatePath(filename);
            copyToDestination(inputStream, destinationFile, filename);
        } catch (IOException e) {
            log.error("Failed to store file: {}. Error: {}", filename, e.getMessage(), e);
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public synchronized void store(String source, String target) {
        store(this.loadAsResource(source), target);
    }

    private Path resolveAndValidatePath(String filename) {
        Path destinationFile = this.rootLocation.resolve(
                        Paths.get(filename))
                .normalize().toAbsolutePath();

        log.debug("Destination path resolved to: {}", destinationFile);

        if (!destinationFile.startsWith(this.rootLocation.toAbsolutePath())) {
            log.error("Security violation: Attempted to store file outside of root location. Path: {}", destinationFile);
            throw new StorageException("Cannot store file outside root directory.");
        }

        // Ensure parent directory exists
        Path parentDir = destinationFile.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            throw new StorageException("Failed to resolve directory: " + parentDir);
        }

        return destinationFile;
    }

    private void copyToDestination(InputStream inputStream, Path destinationFile, String filename) throws IOException {
        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Successfully stored file: {} to location: {}", filename, destinationFile);
    }

    @Override
    public Stream<Path> loadAll(String relativePath) {
        try {
            Path targetDir = this.rootLocation.resolve(relativePath);
            if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
                return Stream.empty();
            }

            return Files.walk(targetDir, 10)
                    .filter(path -> !path.equals(targetDir))
                    .map(targetDir::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files in path: " + relativePath, e);
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
    public synchronized void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public synchronized void delete(String path) {
        try {
            FileSystemUtils.deleteRecursively(rootLocation.resolve(path));
        } catch (IOException e) {
            throw new StorageFileNotFoundException("Could not read directory: " + path);
        }
    }

    @Override
    public synchronized void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public synchronized void createFolder(String name) {
        try {
            Path folderPath = rootLocation.resolve(name);

            Files.createDirectories(folderPath);

            log.debug("Created directory: {}", folderPath);
        } catch (IOException e) {
            throw new StorageException("Could not create folder: " + name, e);
        }
    }

    public static String extractPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
            return path.substring(0, lastSlashIndex + 1);
        }

        return "";
    }
}