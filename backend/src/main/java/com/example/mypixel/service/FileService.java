package com.example.mypixel.service;

import com.example.mypixel.model.FileMetadata;
import com.example.mypixel.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageService storageService;
    private final FileMetadataRepository fileMetadataRepository;

    public void store(MultipartFile file, FileMetadata fileMetadata) {
        storageService.store(file, fileMetadata.getRelativeStoragePath());
        fileMetadataRepository.save(fileMetadata);
    }

    public void store(InputStream inputStream, FileMetadata fileMetadata) {
        storageService.store(inputStream, fileMetadata.getRelativeStoragePath());
        fileMetadataRepository.save(fileMetadata);
    }

    public void store(Resource resource, FileMetadata fileMetadata) {
        storageService.store(resource, fileMetadata.getRelativeStoragePath());
        fileMetadataRepository.save(fileMetadata);
    }

    public Optional<FileMetadata> findById(UUID id) {
        return fileMetadataRepository.findById(id);
    }

    public void createFolder(String path) {
        if (!storageService.folderExists(path)) {
            storageService.createFolder(path);
        }
    }

    public Resource loadAsResource(String path) {
        return storageService.loadAsResource(path);
    }

    public Stream<Path> loadAll(String path) {
        return storageService.loadAll(path);
    }

    public Path getRootLocation() {
        return storageService.getRootLocation();
    }
}
