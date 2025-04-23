package com.example.mypixel.service;

import com.example.mypixel.exception.StorageException;
import com.example.mypixel.model.node.Node;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
        String relativePath = extractRelativeWorkspacePath(filepath);

        if (prefix != null && !prefix.isBlank()) {
            filename = addPrefixToFilename(filepath, prefix);
        }
        if (folder != null) {
            relativePath = folder + "/" + relativePath;
        }

        if (!storageService.folderExists(sceneId + "/output/" + relativePath)) {
            storageService.createFolder(sceneId + "/output/" + relativePath);
        }

        String fullInputPath = storageService.getRootLocation().relativize(Paths.get(filepath)).toString();

        storageService.store(storageService.loadAsResource(fullInputPath),
                sceneId + "/output/" + relativePath + filename);

        return getFullPath(sceneId + "/output/" + relativePath + filename);
    }

    public String storeToTemp(InputStream in, String filepath) {
        Long id = node.getId();

        String path = sceneId + "/temp/" + id + "/" + extractPath(filepath);

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
        String outputPath = sceneId + "/temp/" + node.getId() + "/" + extractRelativeWorkspacePath(filepath);

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

        // Example: {sceneId}/input/folder1/folder2/Picture.jpeg -> folder1/folder2/
        if (pathSegments.contains("input")) index = pathSegments.indexOf("input");
        // Example: {sceneId}/temp/{nodeId}/folder1/folder2/Picture.jpeg -> folder1/folder2/
        if (pathSegments.contains("temp")) index = pathSegments.indexOf("temp") + 1;

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
