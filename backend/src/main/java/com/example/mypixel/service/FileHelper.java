package com.example.mypixel.service;

import com.example.mypixel.exception.StorageException;
import com.example.mypixel.model.FileMetadata;
import com.example.mypixel.model.node.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileHelper {

    FileService fileService;
    Long taskId;
    Long sceneId;
    Node node;

    private final Pattern filenamePattern = Pattern.compile("^(.*?)(\\.[^.]*$|$)");

    public FileHelper(FileService fileService, Node node, Long sceneId, Long taskId) {
        this.fileService = fileService;
        this.sceneId = sceneId;
        this.taskId = taskId;
        this.node = node;
    }

    public String storeToOutput(String id, String folder, String prefix) {
        FileMetadata file = fileService.findById(UUID.fromString(id)).get();
        String filename = extractFilename(file.getName());
        String relativePath = extractPath(file.getName());

        if (prefix != null && !prefix.isBlank()) filename = addPrefixToFilename(filename, prefix);
        if (folder != null) relativePath = folder + "/" + relativePath;

        fileService.createFolder("scenes/" + sceneId + "/output/" + relativePath);

        FileMetadata outputFile = FileMetadata.builder()
                .name(filename)
                .relativeStoragePath("scenes/" + sceneId + "/output/" + relativePath + filename)
                .storagePath(fileService.getRootLocation() + "/scenes/" + sceneId + "/output/" + relativePath + filename)
                .build();

        fileService.store(fileService.loadAsResource(file.getRelativeStoragePath()), outputFile);

        return outputFile.getId().toString();
    }

    public String storeToTemp(InputStream in, String filepath) {
        String path = "tasks/" + taskId + "/" + node.getId() + "/" + extractPath(filepath);

        fileService.createFolder(path);

        FileMetadata file = FileMetadata.builder()
                .name(extractFilename(filepath))
                .relativeStoragePath(path + extractFilename(filepath))
                .storagePath(fileService.getRootLocation() + "/" + path + extractFilename(filepath))
                .build();

        fileService.store(in, file);

        return file.getId().toString();
    }

    public String createDump(String id) {
        FileMetadata file = fileService.findById(UUID.fromString(id)).get();
        String filepath = file.getRelativeStoragePath();

        String actualFilename = file.getName();
        String outputPath = "tasks/" + taskId + "/" + node.getId() + "/" + extractPath(actualFilename);

        fileService.createFolder(outputPath);

        Resource resource = fileService.loadAsResource(filepath);
        if (resource != null) {
            String outputFilePath = outputPath + extractFilename(actualFilename);
            FileMetadata newFile = FileMetadata.builder()
                    .name(actualFilename)
                    .relativeStoragePath(outputFilePath)
                    .storagePath(fileService.getRootLocation() + "/" + outputFilePath)
                    .build();
            fileService.store(resource, newFile);

            return newFile.getId().toString();
        }

        throw new StorageException("Failed to create dump file: Input resource is null for " + filepath);
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
