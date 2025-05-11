package com.example.mypixel.service;

import com.example.mypixel.exception.StorageException;
import com.example.mypixel.model.node.Node;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileHelper {

    StorageService storageService;
    Long taskId;
    Long sceneId;
    Node node;

    private final Pattern filenamePattern = Pattern.compile("^(.*?)(\\.[^.]*$|$)");

    public FileHelper(StorageService storageService, Node node, Long sceneId, Long taskId) {
        this.storageService = storageService;
        this.sceneId = sceneId;
        this.taskId = taskId;
        this.node = node;
    }

    public File createTempJson() {
        String path = "tasks/" + taskId + "/" + node.getId();

        if (!storageService.folderExists(path)) {
            storageService.createFolder(path);
        }

        return new File(storageService.getRootLocation() + "/" + path + "/temp.json");
    }

    public void storeFile(String filepath, String content) {
        String path = "scenes/" + sceneId + "/" + extractPath(filepath);

        if (!storageService.folderExists(path)) {
            storageService.createFolder(path);
        }

        String fullPath = storageService.getRootLocation() + "/" + path + "/" + extractFilename(filepath);

        try {
            Files.write(Paths.get(fullPath), content.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Error writing to file: {}", e.getMessage());
        }
    }

    public String storeToOutput(String filepath, String folder, String prefix) {
        String filename = extractFilename(filepath);
        String relativePath = extractRelativeWorkspacePath(filepath);

        if (prefix != null && !prefix.isBlank()) {
            filename = addPrefixToFilename(filepath, prefix);
        }
        if (folder != null) {
            relativePath = folder + "/" + relativePath;
        }

        if (!storageService.folderExists("scenes/" + sceneId + "/output/" + relativePath)) {
            storageService.createFolder("scenes/" + sceneId + "/output/" + relativePath);
        }

        String fullInputPath = storageService.getRootLocation().relativize(Paths.get(filepath)).toString();

        storageService.store(storageService.loadAsResource(fullInputPath),
                "scenes/" + sceneId + "/output/" + relativePath + filename);

        return getFullPath("scenes/" + sceneId + "/output/" + relativePath + filename);
    }

    public String storeToTemp(InputStream in, String filepath) {
        String path = "tasks/" + taskId + "/" + node.getId() + "/" + extractPath(filepath);

        if (!storageService.folderExists(path)) {
            storageService.createFolder(path);
        }

        storageService.store(in, path + extractFilename(filepath));

        return getFullPath(path + extractFilename(filepath));
    }

    public String getFullPath(String filepath) {
        return storageService.load(filepath).toString();
    }

    public String createDump(String filepath) {
        String actualFilename = extractFilename(filepath);
        String outputPath = "tasks/" + taskId + "/" + node.getId() + "/" + extractRelativeWorkspacePath(filepath);

        if (!storageService.folderExists(outputPath)) {
            storageService.createFolder(outputPath);
        }

        String fullInputPath = storageService.getRootLocation().relativize(Paths.get(filepath)).toString();

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
        if (lastSlashIndex == path.length() - 1) return "";

        return path;
    }

    public String extractPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
            return path.substring(0, lastSlashIndex + 1);
        }

        return "";
    }

    public String extractRelativeWorkspacePath(String filepath) {
        List<String> pathSegments = Splitter.on("/").splitToList(filepath);
        int index = -1;
        // Example: scenes/{sceneId}/input/folder1/folder2/Picture.jpeg -> folder1/folder2/
        if (pathSegments.contains("scenes")) index = pathSegments.indexOf("scenes") + 2;
        // Example: tasks/{taskId}/{nodeId}/folder1/folder2/Picture.jpeg -> folder1/folder2/
        if (pathSegments.contains("tasks")) index = pathSegments.indexOf("tasks") + 2;

        StringBuilder insideInputPathBuilder = new StringBuilder();
        if (index != -1 && index < pathSegments.size() - 1) {
            for (int i = index + 1; i < pathSegments.size()  - 1; i++) {
                insideInputPathBuilder.append(pathSegments.get(i));
                if (i < pathSegments.size() - 1) {
                    insideInputPathBuilder.append("/");
                }
            }
        }
        return insideInputPathBuilder.toString();
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
