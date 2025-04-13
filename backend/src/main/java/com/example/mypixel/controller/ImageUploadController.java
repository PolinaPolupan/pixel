package com.example.mypixel.controller;


import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.example.mypixel.exception.InvalidImageFormat;
import com.example.mypixel.service.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RestController
@RequestMapping("/v1/scene/{sceneId}/image")
public class ImageUploadController {

    private final FileManager fileManager;

    @Autowired
    public ImageUploadController(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @GetMapping("/")
    public List<String> listUploadedFiles(@PathVariable String sceneId) {
        return fileManager.loadAll(sceneId).map(
                        path -> MvcUriComponentsBuilder.fromMethodName(ImageUploadController.class,
                                "serveFile", sceneId, path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String sceneId, @PathVariable String filename) {
        Resource file = fileManager.loadAsResource(filename, sceneId);

        if (file == null)
            return ResponseEntity.notFound().build();

        String contentType;
        try {
            contentType = Files.probeContentType(Paths.get(file.getFile().getAbsolutePath()));
        } catch (IOException e) {
            contentType = URLConnection.guessContentTypeFromName(file.getFilename());
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public ResponseEntity<Void> handleFileUpload(@PathVariable String sceneId,
                                                 @RequestParam("file") List<MultipartFile> files) {

        for (MultipartFile file: files) {
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new InvalidImageFormat("Only JPEG or PNG images are allowed");
            }

            fileManager.store(file, sceneId);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
