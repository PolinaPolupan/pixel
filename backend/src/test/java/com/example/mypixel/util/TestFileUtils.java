package com.example.mypixel.util;


import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class TestFileUtils {

    public static void copyResourceToServerDirectory(String resourcePath, String targetDirectory, String targetFilename) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);

            Path targetDir = Paths.get(targetDirectory);
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(targetFilename);

            try (InputStream in = resource.getInputStream();
                 FileOutputStream out = new FileOutputStream(targetPath.toFile())) {
                FileCopyUtils.copy(in, out);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to copy test resource: " + e.getMessage(), e);
        }
    }

    public static void copyResourcesToDirectory(String targetDirectory, String... resources) {
        for (String resource : resources) {
            String filename = new File(resource).getName();
            copyResourceToServerDirectory(resource, targetDirectory, filename);
        }
    }
}
