package com.example.mypixel.service;

import com.example.mypixel.exception.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;



@Service
public class FileManager {

    private final StorageService storageService;
    private final Pattern filenamePattern = Pattern.compile("^(.*?)(\\.[^.]*$|$)");
    private final Pattern prefixPattern = Pattern.compile("^[^_]+_(.*?)(\\.[^.]*$|$)");

    @Autowired
    public FileManager(StorageService storageService) {
        this.storageService = storageService;
    }

    public void createScene(String sceneId) {
        storageService.createFolder(sceneId);
    }

    public boolean sceneExists(String sceneId) {
        return storageService.folderExists(sceneId);
    }

    public void store(MultipartFile file, String sceneId) {
        storageService.store(file, sceneId + "/" + file.getOriginalFilename());
    }

    public void store(Resource resource, String sceneId) {
        store(resource, sceneId, resource.getFilename());
    }

    public void store(Resource resource, String sceneId, String filename) {
        storageService.store(resource, sceneId + "/" + filename);
    }

    public void store(InputStream in, String sceneId, String filename) {
        storageService.store(in, sceneId + "/" + filename);
    }

    public Path load(String filename, String sceneId) {
        return storageService.load(sceneId + "/" + filename);
    }

    public Resource loadAsResource(String filename, String sceneId) {
        return storageService.loadAsResource(sceneId + "/" + filename);
    }

    public Stream<Path> loadAll(String sceneId) {
        return storageService.loadAll(sceneId);
    }

    public String createDump(String filename, String sceneId) {
        Resource resource = loadAsResource(filename, sceneId);
        if (resource != null) {
            String tempName = addPrefixToFilename(resource.getFilename(), UUID.randomUUID().toString());
            store(resource, sceneId, tempName);
            return tempName;
        }
        throw new StorageException("Failed to create temp file: Input resource is null");
    }

    public String addPrefixToFilename(String filename, String prefix) {
        filename = removeExistingPrefix(filename);

        Matcher matcher = filenamePattern.matcher(filename);
        if (matcher.find()) {
            String baseName = matcher.group(1);
            String extension = matcher.group(2);
            return prefix + "_" + baseName + extension;
        }
        return filename;
    }

    public String removeExistingPrefix(String filename) {
        Matcher matcher = prefixPattern.matcher(filename);
        if (matcher.find()) {
            String baseName = matcher.group(1);
            String extension = matcher.group(2);
            return baseName + extension;
        }
        return filename;
    }
}