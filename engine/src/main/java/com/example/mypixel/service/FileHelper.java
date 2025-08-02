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

    public static String storeFromWorkspaceToScene(Long sceneId, String source, String folder, String prefix) {
        String filename = extractFilename(source);
        String relativePath = extractRelativeWorkspacePath(source);

        if (prefix != null && !prefix.isBlank()) filename = addPrefixToFilename(source, prefix);
        if (folder != null) relativePath = folder + "/" + relativePath;

        String target = getSceneContext(sceneId) + relativePath + filename;

        storageService.store(source, target);

        return target;
    }

    public static String storeToTask(Long taskId, Long nodeId, InputStream in, String target) {
        String path = getTaskContext(taskId, nodeId) + target;

        storageService.store(in, path);

        return path;
    }

    public static String storeFromWorkspaceToTask(Long taskId, Long nodeId, String source) {
        String target = getTaskContext(taskId, nodeId) + extractRelativeWorkspacePath(source) + extractFilename(source);

        storageService.store(source, target);

        return target;
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
