package com.example.pixel.file_system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/storage")
@Slf4j
public class StorageController {

    private final FileHelper fileHelper;

    @Autowired
    public StorageController(FileHelper fileHelper) {
        this.fileHelper = fileHelper;
    }

    @PostMapping("/workspace-to-scene")
    public ResponseEntity<Map<String, String>> storeFromWorkspaceToScene(
            @RequestParam("sceneId") Long sceneId,
            @RequestParam("source") String source,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "prefix", required = false) String prefix
    ) {
        String targetPath = fileHelper.storeFromWorkspaceToScene(sceneId, source, folder, prefix);

        Map<String, String> response = new HashMap<>();
        response.put("path", targetPath);
        response.put("message", "File stored successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/to-task", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> storeToTask(
            @RequestParam("taskId") Long taskId,
            @RequestParam("nodeId") Long nodeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("target") String target
    ) throws IOException {
        String targetPath = fileHelper.storeToTask(taskId, nodeId, file.getInputStream(), target);

        Map<String, String> response = new HashMap<>();
        response.put("path", targetPath);
        response.put("message", "File stored successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/workspace-to-task")
    public ResponseEntity<Map<String, String>> storeFromWorkspaceToTask(
            @RequestParam("taskId") Long taskId,
            @RequestParam("nodeId") Long nodeId,
            @RequestParam("source") String source
    ) {
        String targetPath = fileHelper.storeFromWorkspaceToTask(taskId, nodeId, source);

        Map<String, String> response = new HashMap<>();
        response.put("path", targetPath);
        response.put("message", "File stored successfully");

        return ResponseEntity.ok(response);
    }
}