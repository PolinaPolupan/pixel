package com.example.mypixel.service;

import com.example.mypixel.exception.StorageException;
import com.example.mypixel.model.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
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
        storageService.createFolder(sceneId + "/temp");
        storageService.createFolder(sceneId + "/output");
        storageService.createFolder(sceneId + "/input");
    }

    public void store(MultipartFile file, String path) {
        storageService.store(file, path);
    }

    public void store(Resource resource, String path) {
        storageService.store(resource, path);
    }

    public void store(InputStream in, String path) {
        storageService.store(in, path);
    }

    public Path getFullPath(String filename) {
        return storageService.load(filename);
    }

    public Resource loadAsResource(String filename) {
        return storageService.loadAsResource(filename);
    }

    public Stream<Path> loadAll(String path) {
        return storageService.loadAll(path);
    }

    public boolean folderExists(String path) {
        return storageService.folderExists(path);
    }

    public void createFolder(String path) {
        storageService.createFolder(path);
    }

    public String createDump(Node node, String filename) {
        String sceneId = (String) node.getInputs().getOrDefault("sceneId", 0);

        String outputPath;

        String actualFilename = extractFilename(filename);

        outputPath = sceneId + "/temp/" + node.getId() + "/";

        if (!storageService.folderExists(outputPath)) {
            storageService.createFolder(outputPath);
        }

        // Determine the correct input path based on whether filename contains path components
        String fullInputPath;
        if (filename.contains("/")) {
            // If filename already includes path components, use it directly
            fullInputPath = filename;
        } else {
            // Otherwise, combine input path with filename
            fullInputPath = sceneId + "/input/" + filename;
        }

        Resource resource = storageService.loadAsResource(fullInputPath);
        if (resource != null) {
            // Store the resource using only the actual filename in the output path
            String outputFilePath = outputPath + actualFilename;
            storageService.store(resource, outputFilePath);
            return outputFilePath;
        }

        throw new StorageException("Failed to create dump file: Input resource is null for " + fullInputPath);
    }

    public String extractFilename(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
            return path.substring(lastSlashIndex + 1);
        }

        return path;
    }

    public String addPrefixToFilename(String filename, String prefix) {
        filename = extractFilename(filename);
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