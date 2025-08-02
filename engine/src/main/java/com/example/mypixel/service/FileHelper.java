package com.example.mypixel.service;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileHelper {

    private static StorageService storageService;

    private static final Pattern filenamePattern = Pattern.compile("^(.*?)(\\.[^.]*$|$)");

    public static void setStorageService(StorageService storageService) {
        FileHelper.storageService = storageService;
    }

    public static String storeFromTaskToSceneContext(Long sceneId, String filepath, String folder, String prefix) {
        String filename = extractFilename(filepath);
        String relativePath = extractRelativeWorkspacePath(filepath);

        if (prefix != null && !prefix.isBlank()) filename = addPrefixToFilename(filepath, prefix);
        if (folder != null) relativePath = folder + "/" + relativePath;

        storageService.store(filepath, getSceneContext(sceneId) + relativePath + filename);

        return getSceneContext(sceneId) + relativePath + filename;
    }

    public static String storeToTaskContext(Long taskId, Long nodeId, InputStream in, String filepath) {
        String path = getTaskContext(taskId, nodeId) + extractPath(filepath);

        storageService.store(in, path + extractFilename(filepath));

        return path + extractFilename(filepath);
    }

    public static String storeFromTaskToTaskContext(Long taskId, Long nodeId, String filepath) {
        String actualFilename = extractFilename(filepath);
        String outputPath = getTaskContext(taskId, nodeId) + extractRelativeWorkspacePath(filepath);

        String outputFilePath = outputPath + actualFilename;
        storageService.store(filepath, outputFilePath);

        return outputFilePath;
    }

    private static String getTaskContext(Long taskId, Long nodeId) {
        return  "tasks/" + taskId + "/" + nodeId + "/";
    }

    private static String getSceneContext(Long sceneId) {
        return  "scenes/" + sceneId + "/";
    }

    public static String extractFilename(String path) {
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

    public static String extractRelativeWorkspacePath(String filepath) {
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

    public static String addPrefixToFilename(String filename, String prefix) {
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
