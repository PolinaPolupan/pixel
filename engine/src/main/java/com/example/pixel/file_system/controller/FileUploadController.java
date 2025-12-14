package com.example.pixel.file_system.controller;

import java. io.*;
import java.net. URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip. ZipEntry;
import java. util.zip.ZipInputStream;
import java.util.zip. ZipOutputStream;

import com. example.pixel.common.exception. InvalidFileFormat;
import com. example.pixel.file_system. util.FileHelper;
import com.example.pixel.file_system.dto.FileStats;
import com.example.pixel.file_system.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework. core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework. http.HttpStatus;
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
    public ResponseEntity<String> storeToOutput(
            @RequestParam("source") String source,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "prefix", required = false) String prefix
    ) {
        String targetPath = fileHelper.storeToOutput(source, folder, prefix);
        return ResponseEntity.ok(targetPath);
    }

    @PostMapping("/dump")
    public ResponseEntity<String> storeToDump(
            @RequestParam("source") String source,
            @RequestParam("graphExecutionId") Long graphExecutionId,
            @RequestParam("nodeId") Long nodeId
    ) {
        String targetPath = fileHelper.storeToDump(graphExecutionId, nodeId, source);
        return ResponseEntity.ok(targetPath);
    }

    @GetMapping(path = "/list")
    public List<String> listFiles(
            @RequestParam(required = false, defaultValue = "") String folder,
            @RequestParam(required = false) Long graphExecutionId,
            @RequestParam(required = false) Long nodeId
    ) {
        if (graphExecutionId != null && nodeId != null) {
            return fileHelper.getDump(graphExecutionId, nodeId);
        } else {
            return fileHelper.getFilePaths(folder);
        }
    }

    @GetMapping(path = "/zip", produces = "application/zip")
    public ResponseEntity<byte[]> zipUploadedFiles(
            @RequestParam(required = false, defaultValue = "") String folder,
            @RequestParam(required = false) Long graphExecutionId,
            @RequestParam(required = false) Long nodeId
    ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (graphExecutionId != null && nodeId != null) {
            // Zip node execution files
            List<String> files = fileHelper.getDump(graphExecutionId, nodeId);
            Path dumpBase = fileHelper.getDumpBasePath(graphExecutionId, nodeId);

            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (String relativePath : files) {
                    Path absolutePath = dumpBase. resolve(relativePath);

                    if (Files.exists(absolutePath) && ! Files.isDirectory(absolutePath)) {
                        ZipEntry entry = new ZipEntry(relativePath);
                        zos.putNextEntry(entry);
                        Files.copy(absolutePath, zos);
                        zos.closeEntry();
                    }
                }
            }

            String filename = String.format("node-%d-exec-%d.zip", nodeId, graphExecutionId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType. APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());
        } else {
            // Zip regular storage files
            List<String> relativePaths = storageService.loadAll(folder)
                    . map(Path::toString)
                    .toList();

            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                Path baseDir = this.storageService.getRootLocation().resolve(folder);

                for (String relativePath : relativePaths) {
                    Path absolutePath = baseDir.resolve(relativePath);

                    if (!Files. isDirectory(absolutePath)) {
                        ZipEntry entry = new ZipEntry(relativePath);
                        zos.putNextEntry(entry);
                        Files.copy(absolutePath, zos);
                        zos.closeEntry();
                    }
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());
        }
    }

    @GetMapping(path = "/files")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@RequestParam String filepath) {
        log.info("Serving file: {}", filepath);

        Path filePath = storageService.getRootLocation().resolve(filepath);

        if (!Files.exists(filePath)) {
            log.warn("File not found: {}", filePath);
            return ResponseEntity.notFound().build();
        }

        Resource file = new FileSystemResource(filePath);

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = URLConnection.guessContentTypeFromName(file.getFilename());
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity. ok()
                .contentType(MediaType.valueOf(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(file);
    }

    @PostMapping("/upload")
    public ResponseEntity<FileStats> handleUpload(@RequestParam("file") List<MultipartFile> files) throws IOException {
        List<String> locations = new ArrayList<>();

        for (MultipartFile file :  files) {
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

    private FileStats calculateFileStats(List<String> locations) {
        int zipFiles = 0;
        int imageFiles = 0;
        long totalSize = 0;

        for (String location : locations) {
            String filename = location.substring(location.lastIndexOf('/') + 1);
            Path filePath = this. storageService.getRootLocation().resolve(location);

            try {
                totalSize += Files.size(filePath);

                if (filename.toLowerCase().endsWith(".zip")) {
                    zipFiles++;
                } else if (filename. toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                    imageFiles++;
                }
            } catch (IOException e) {
                log.warn("Could not determine size of file: {}", location, e);
            }
        }

        return FileStats.builder()
                .totalFiles(locations.size())
                .totalSize(totalSize)
                .zipFiles(zipFiles)
                .imageFiles(imageFiles)
                .locations(locations)
                .build();
    }
}