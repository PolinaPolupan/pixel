package com.example.mypixel.service;

import com.example.mypixel.exception.StorageException;
import com.example.mypixel.model.node.Node;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHelper {

    StorageService storageService;
    String sceneId;
    Node node;

    private final Pattern filenamePattern = Pattern.compile("^(.*?)(\\.[^.]*$|$)");

    public FileHelper(StorageService storageService, Node node) {
        this.storageService = storageService;
        this.sceneId = node.getSceneId();
        this.node = node;
    }

    public String storeToOutput(String filepath, String folder, String prefix) {
        String filename = extractFilename(filepath);
        if (prefix != null) {
            filename = addPrefixToFilename(filepath, prefix);
        }
        if (folder != null) {
            if (!storageService.folderExists(sceneId + "/output/" + folder)) {
                storageService.createFolder(sceneId + "/output/" + folder);
            }
            filename = folder + "/" + filename;
        }

        String fullInputPath = storageService.getRootLocation().relativize(Paths.get(filepath)).toString();

        storageService.store(storageService.loadAsResource(fullInputPath), sceneId + "/output/" + filename);

        return getFullPath(sceneId + "/output/" + filename);
    }

    public String storeToTemp(InputStream in, String filename) {
        Long id = node.getId();

        if (!storageService.folderExists(sceneId + "/temp/" + id)) {
            storageService.createFolder(sceneId + "/temp/" + id);
        }

        storageService.store(in, sceneId + "/temp/" + id + "/" + filename);

        return getFullPath(sceneId + "/temp/" + id + "/" + filename);
    }

    public String getFullPath(String filepath) {
        return storageService.load(filepath).toString();
    }

    public String createDump(String filepath) {
        String actualFilename = extractFilename(filepath);
        String outputPath = sceneId + "/temp/" + node.getId() + "/";

        if (!storageService.folderExists(outputPath)) {
            storageService.createFolder(outputPath);
        }

        // Determine the correct input path based on whether filename contains path components
        String fullInputPath;
        if (filepath.contains("/")) {
            // If filename already includes path components, use it directly
            fullInputPath = storageService.getRootLocation().relativize(Paths.get(filepath)).toString();
        } else {
            // Otherwise, combine input path with filename
            fullInputPath = sceneId + "/input/" + filepath;
        }

        Resource resource = storageService.loadAsResource(fullInputPath);
        if (resource != null) {
            String outputFilePath = outputPath + actualFilename;
            storageService.store(resource, outputFilePath);

            return getFullPath(outputFilePath);
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

        Matcher matcher = filenamePattern.matcher(filename);
        if (matcher.find()) {
            String baseName = matcher.group(1);
            String extension = matcher.group(2);
            return prefix + "_" + baseName + extension;
        }
        return filename;
    }
}
