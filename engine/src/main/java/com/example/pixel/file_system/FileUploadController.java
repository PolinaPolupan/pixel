package com.example.pixel.file_system;


import java.io.*;
import java.net.URLConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.example.pixel.exception.InvalidFileFormat;
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
@RequestMapping("/v1/storage")
@Slf4j
@RequiredArgsConstructor
public class FileUploadController {

    private final StorageService storageService;
    private final FileHelper fileHelper;

    @PostMapping("/output")
    public ResponseEntity<Map<String, String>> storeToOutput(
            @RequestParam("source") String source,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "prefix", required = false) String prefix
    ) {
        String targetPath = fileHelper.storeToOutput(source, folder, prefix);

        Map<String, String> response = new HashMap<>();
        response.put("path", targetPath);
        response.put("message", "File stored successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/task")
    public ResponseEntity<Map<String, String>> storeToTask(
            @RequestParam("source") String source,
            @RequestParam("taskId") Long taskId,
            @RequestParam("nodeId") Long nodeId
    ) {
        String targetPath = fileHelper.storeToTask(taskId, nodeId, source);

        Map<String, String> response = new HashMap<>();
        response.put("path", targetPath);
        response.put("message", "File stored successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/list", produces = "application/json")
    public FileStatsPayload listUploadedFiles(@RequestParam(required = false, defaultValue = "") String folder) {
        List<String> locations = storageService.loadAll(folder)
                .map((path) -> folder + path.toString())
                .collect(Collectors.toList());

        return calculateFileStats(locations);
    }

    @GetMapping(path = "/zip", produces = "application/zip")
    public byte[] zipUploadedFiles(@RequestParam(required = false, defaultValue = "") String folder) throws IOException {
        List<String> relativePaths = storageService.loadAll(folder)
                .map(Path::toString)
                .toList();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Path baseDir = this.storageService.getRootLocation().resolve(folder);

            for (String relativePath : relativePaths) {
                Path absolutePath = baseDir.resolve(relativePath);

                if (!Files.isDirectory(absolutePath)) {
                    ZipEntry entry = new ZipEntry(relativePath);
                    zos.putNextEntry(entry);

                    Files.copy(absolutePath, zos);

                    zos.closeEntry();
                }
            }
        }

        return baos.toByteArray();
    }

    @GetMapping(path ="/file")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@RequestParam String filepath) {
        Resource file = storageService.loadAsResource(filepath);
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

    @PostMapping("/upload")
    public ResponseEntity<FileStatsPayload> handleUpload(@RequestParam("file") List<MultipartFile> files) throws IOException {
        List<String> locations = new ArrayList<>();

        for (MultipartFile file: files) {
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new InvalidFileFormat("");
            }
            if (contentType.equals("application/zip") || contentType.equals("application/x-zip-compressed")) {
                locations.addAll(storeZip(file));
            } else {
                storageService.store(file);
                locations.add(file.getOriginalFilename());
            }
        }

        return new ResponseEntity<>(calculateFileStats(locations), HttpStatus.CREATED);
    }

    private List<String> storeZip(MultipartFile file) throws IOException {
        List<String> locations = new ArrayList<>();
        ZipInputStream inputStream = new ZipInputStream(file.getInputStream());

        String zipFolderName = file.getOriginalFilename();
        if (zipFolderName != null && zipFolderName.toLowerCase().endsWith(".zip")) {
            zipFolderName = zipFolderName.substring(0, zipFolderName.length() - 4);
        }

        storageService.createFolder(zipFolderName);

        for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null; ) {
            if (entry.isDirectory()) {
                storageService.createFolder(zipFolderName + "/" + entry.getName());
            } else {
                storageService.store(inputStream, zipFolderName + "/" + entry.getName());
                locations.add(zipFolderName + "/" + entry.getName());
            }
        }

        return locations;
    }

    private FileStatsPayload calculateFileStats(List<String> locations) {
        int zipFiles = 0;
        int imageFiles = 0;
        long totalSize = 0;

        for (String location : locations) {
            String filename = location.substring(location.lastIndexOf('/') + 1);
            Path filePath = this.storageService.getRootLocation().resolve(location);

            try {
                totalSize += Files.size(filePath);

                if (filename.toLowerCase().endsWith(".zip")) {
                    zipFiles++;
                } else if (filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                    imageFiles++;
                }
            } catch (IOException e) {
                log.warn("Could not determine size of file: {}", location, e);
            }
        }

        return FileStatsPayload.builder()
                .totalFiles(locations.size())
                .totalSize(totalSize)
                .zipFiles(zipFiles)
                .imageFiles(imageFiles)
                .locations(locations)
                .build();
    }
}