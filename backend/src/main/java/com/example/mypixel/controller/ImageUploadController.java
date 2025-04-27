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

    @GetMapping(path = "/input/list", produces = "application/json")
    public List<String> listUploadedFiles(@PathVariable String sceneId,
                                          @RequestParam(required = false, defaultValue = "") String folder) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        return storageService.loadAll(sceneId + "/input/" + folder).map(Path::toString).collect(Collectors.toList());
    }

    @GetMapping(path = "/output/list", produces = "application/json")
    public List<String> listOutputFiles(@PathVariable String sceneId,
                                        @RequestParam(required = false, defaultValue = "") String folder) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        return storageService.loadAll(sceneId + "/output/" + folder).map(Path::toString).collect(Collectors.toList());
    }

    @GetMapping(path ="/input/file", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE
    })
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String sceneId, @RequestParam String filepath) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        Resource file = storageService.loadAsResource(sceneId + "/input/" + filepath);

        return getResourceResponseEntity(file);
    }

    @GetMapping(path = "/output/file", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE
    })
    @ResponseBody
    public ResponseEntity<Resource> serveOutputFile(@PathVariable String sceneId, @RequestParam String filepath) {
        sceneService.updateLastAccessed(Long.valueOf(sceneId));
        Resource file = storageService.loadAsResource(sceneId + "/output/" + filepath);

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
    public ResponseEntity<List<String>> handleFileUpload(@PathVariable String sceneId,
                                                                      @RequestParam("file") List<MultipartFile> files) throws IOException {

        List<String> locations = new ArrayList<>();

        for (MultipartFile file: files) {
            String contentType = file.getContentType();
            switch (Objects.requireNonNull(contentType)) {
                case "image/jpeg", "image/png": {
                    storageService.store(file, sceneId + "/input/" + file.getOriginalFilename());
                    locations.add(storageService.load(sceneId + "/input/" + file.getOriginalFilename()).toString());
                    break;
                }
                case "application/zip", "application/x-zip-compressed": {
                    ZipInputStream inputStream = new ZipInputStream(file.getInputStream());

                    String zipFolderName = file.getOriginalFilename();
                    if (zipFolderName != null && zipFolderName.toLowerCase().endsWith(".zip")) {
                        zipFolderName = zipFolderName.substring(0, zipFolderName.length() - 4);
                    }

                    storageService.createFolder(sceneId + "/input/" + zipFolderName);

                    for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null; ) {
                        Path entryPath = Path.of(entry.getName());
                        if (entry.isDirectory()) {
                            storageService.createFolder(sceneId + "/input/" + zipFolderName + "/" + entry.getName());
                        } else {
                            if (entryPath.getParent() != null) {
                                storageService.createFolder(sceneId + "/input/" + zipFolderName + "/" + entryPath.getParent());
                            }
                            String extension = com.google.common.io.Files.getFileExtension(entry.getName());
                            if (!extension.equals("png") && !extension.equals("jpeg") && !extension.equals("jpg")) {
                                log.warn("Couldn't process file {}. Only JPEG, PNG and ZIP files are allowed", entry.getName());
                            } else {
                                storageService.store(inputStream, sceneId + "/input/" + zipFolderName + "/" + entry.getName());
                                locations.add(storageService.load(sceneId + "/input/" + zipFolderName + "/" + entry.getName()).toString());
                            }
                        }
                    }
                    break;
                }
                default:
                    throw new InvalidImageFormat("Only JPEG, PNG and ZIP files are allowed");
            }
        }

        return new ResponseEntity<>(locations, HttpStatus.CREATED);
    }
}