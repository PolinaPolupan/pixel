package com.example.mypixel.controller;


import java.io.IOException;
import java.net.URLConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.example.mypixel.exception.InvalidImageFormat;
import com.example.mypixel.model.FileMetadata;
import com.example.mypixel.service.FileService;
import com.example.mypixel.service.SceneService;
import com.example.mypixel.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/scene/{sceneId}")
@Slf4j
@RequiredArgsConstructor
public class ImageUploadController {

    private final SceneService sceneService;
    private final StorageService storageService;
    private final FileService fileService;

    @GetMapping(path = "/input/list", produces = "application/json")
    public List<String> listUploadedFiles(@PathVariable String sceneId,
                                          @RequestParam(required = false, defaultValue = "") String folder) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        return storageService.loadAll("scenes/" + sceneId + "/input/" + folder).map(Path::toString).collect(Collectors.toList());
    }

    @GetMapping(path = "/output/list", produces = "application/json")
    public List<String> listOutputFiles(@PathVariable String sceneId,
                                        @RequestParam(required = false, defaultValue = "") String folder) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        return storageService.loadAll("scenes/" + sceneId + "/output/" + folder).map(Path::toString).collect(Collectors.toList());
    }

    @GetMapping(path ="/input/file", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE
    })
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String sceneId, @RequestParam String filepath) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        Resource file = storageService.loadAsResource("scenes/" + sceneId + "/input/" + filepath);

        return getResourceResponseEntity(file);
    }

    @GetMapping(path = "/output/file", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE
    })
    @ResponseBody
    public ResponseEntity<Resource> serveOutputFile(@PathVariable String sceneId, @RequestParam String filepath) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        Resource file = storageService.loadAsResource("scenes/" +sceneId + "/output/" + filepath);

        return getResourceResponseEntity(file);
    }

    private ResponseEntity<Resource> getResourceResponseEntity(Resource file) {
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

    @PostMapping("/input")
    public ResponseEntity<List<UUID>> handleFileUpload(
            @PathVariable String sceneId,
            @RequestParam("file") List<MultipartFile> files) throws IOException {
        List<UUID> ids = new ArrayList<>();
        String basePath = "scenes/" + sceneId + "/input/";

        for (MultipartFile file: files) {
            String contentType = file.getContentType();
            switch (Objects.requireNonNull(contentType)) {
                case "image/jpeg", "image/png": {
                    FileMetadata fileMetadata = FileMetadata
                            .builder()
                            .name(file.getOriginalFilename())
                            .relativeStoragePath(basePath + file.getOriginalFilename())
                            .storagePath(storageService.getRootLocation() + "/" + basePath + file.getOriginalFilename())
                            .build();
                    fileService.store(file, fileMetadata);
                    ids.add(fileMetadata.getId());
                    break;
                }
                case "application/zip", "application/x-zip-compressed": {
                    ZipInputStream inputStream = new ZipInputStream(file.getInputStream());

                    String zipFolderName = file.getOriginalFilename();
                    if (zipFolderName != null && zipFolderName.toLowerCase().endsWith(".zip")) {
                        zipFolderName = zipFolderName.substring(0, zipFolderName.length() - 4);
                    }

                    storageService.createFolder(basePath + zipFolderName);

                    for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null; ) {
                        Path entryPath = Path.of(entry.getName());
                        if (entry.isDirectory()) {
                            storageService.createFolder( basePath+ zipFolderName + "/" + entry.getName());
                        } else {
                            if (entryPath.getParent() != null) {
                                storageService.createFolder(basePath + zipFolderName + "/" + entryPath.getParent());
                            }
                            String extension = com.google.common.io.Files.getFileExtension(entry.getName());
                            if (!extension.equals("png") && !extension.equals("jpeg") && !extension.equals("jpg")) {
                                log.warn("Couldn't process file {}. Only JPEG, PNG and ZIP files are allowed", entry.getName());
                            } else {
                                FileMetadata fileMetadata = FileMetadata
                                        .builder()
                                        .name(entry.getName())
                                        .relativeStoragePath(basePath + zipFolderName + "/" + entry.getName())
                                        .storagePath(storageService.getRootLocation() + "/" + basePath + zipFolderName + "/" + entry.getName())
                                        .build();
                                fileService.store(inputStream, fileMetadata);
                                ids.add(fileMetadata.getId());
                            }
                        }
                    }
                    break;
                }
                default:
                    throw new InvalidImageFormat("Only JPEG, PNG and ZIP files are allowed");
            }
        }

        return new ResponseEntity<>(ids, HttpStatus.CREATED);
    }
}