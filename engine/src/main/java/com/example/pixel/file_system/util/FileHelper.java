package com.example.pixel.file_system.util;

import com.example.pixel.file_system.service.StorageService;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FileHelper {

    private final StorageService storageService;

    @Value("${dump.directory}")
    private String dumpDir;

    private final Pattern filenamePattern = Pattern.compile("^(.*?)(\\.[^.]*$|$)");

    @Autowired
    public FileHelper(StorageService storageService) {
        this.storageService = storageService;
    }

    public String storeToOutput(String source, String folder, String prefix) {
        String filename = extractFilename(source);
        String relativePath = extractRelativeWorkspacePath(source);

        if (prefix != null && !prefix.isBlank()) filename = addPrefixToFilename(source, prefix);
        if (folder != null) relativePath = folder + "/" + relativePath;

        String target = relativePath + filename;

        storageService.store(source, target);

        return target;
    }

    public String storeToDump(Long graphExecutionId, Long nodeId, String source) {
        String target = getDumpContext(graphExecutionId, nodeId) + extractRelativeWorkspacePath(source) + extractFilename(source);

        storageService.store(source, target);

        return target;
    }

    public List<String> getDump(Long graphExecutionId, Long nodeId) {
        return getFilePaths(getDumpContext(graphExecutionId, nodeId));
    }

    public List<String> getFilePaths(String folder) {
        return storageService.loadAll(folder)
                .map((path) -> folder + path.toString())
                .collect(Collectors.toList());
    }

    private String getDumpContext(Long taskId, Long nodeId) {
        return  dumpDir + "/" + taskId + "/" + nodeId + "/";
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

    public String extractRelativeWorkspacePath(String filepath) {
        List<String> pathSegments = Splitter.on("/").splitToList(filepath);
        int index = -1;
        // Example: dump/{taskId}/{nodeId}/folder1/folder2/Picture.jpeg -> folder1/folder2/
        if (pathSegments.contains(dumpDir)) index = pathSegments.indexOf(dumpDir) + 2;

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
