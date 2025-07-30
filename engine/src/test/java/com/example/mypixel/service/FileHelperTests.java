package com.example.mypixel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.mypixel.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
public class FileHelperTests {

    @Mock
    private StorageService storageService;

    @Mock
    private Resource resource;

    private static final Long SCENE_ID = 123L;
    private static final Long TASK_ID = 123L;
    private static final Long NODE_ID = 456L;

    @BeforeEach
    void setUp() {
       FileHelper.setStorageService(storageService);
    }

    @Nested
    @DisplayName("Extract Filename Tests")
    class ExtractFilenameTests {

        @ParameterizedTest
        @CsvSource({
                "/path/to/file.jpg, file.jpg",
                "file.jpg, file.jpg",
                "/path/with/multiple/slashes/file.jpg, file.jpg",
                "path/to/filename.with.dots.jpg, filename.with.dots.jpg"
        })
        void shouldExtractFilenameCorrectly(String input, String expected) {
            assertEquals(expected, FileHelper.extractFilename(input));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldHandleNullAndEmptyInput(String input) {
            assertEquals("", FileHelper.extractFilename(input));
        }

        @Test
        void shouldHandlePathEndingWithSlash() {
            assertEquals("", FileHelper.extractFilename("/path/to/directory/"));
        }
    }

    @Nested
    @DisplayName("Extract Path Tests")
    class ExtractPathTests {

        @Test
        void shouldExtractPath() {
            String filepath = "123/input/pic/Picture.jpeg";
            assertEquals("123/input/pic/", FileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractEmptyPath() {
            String filepath = "Picture.jpeg";
            assertEquals("", FileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractPathAfterInput() {
            String filepath = "scenes/" + SCENE_ID + "/input/pic/Picture.jpeg";
            assertEquals("pic/", FileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldExtractPathAfterId() {
            String filepath = "tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture.jpeg";
            assertEquals("output/", FileHelper.extractRelativeWorkspacePath(filepath));
        }


        @Test
        void shouldHandleMultipleSubfoldersAfterInput() {
            String filepath = "scenes/" + SCENE_ID + "/input/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", FileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleMultipleSubfoldersAfter() {
            String filepath = "tasks/" + TASK_ID + "/" + NODE_ID + "/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", FileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleInputAsLastSegment() {
            String filepath = "scenes" + SCENE_ID + "/input";
            assertEquals("", FileHelper.extractRelativeWorkspacePath(filepath));
        }
    }

    @Nested
    @DisplayName("Add Prefix To Filename Tests")
    class AddPrefixToFilenameTests {

        @ParameterizedTest
        @CsvSource({
                "file.jpg, prefix, prefix_file.jpg",
                "/path/to/file.jpg, prefix, prefix_file.jpg",
                "file.with.dots.jpg, pre, pre_file.with.dots.jpg",
                "file, prefix, prefix_file"
        })
        void shouldAddPrefixCorrectly(String filename, String prefix, String expected) {
            assertEquals(expected, FileHelper.addPrefixToFilename(filename, prefix));
        }
    }

    @Nested
    @DisplayName("Store To Output Tests")
    class StoreToOutputTests {

        @Test
        void shouldStoreFileToOutputWithoutFolderOrPrefix() {
            String filepath = "/root/path/scenes/" + SCENE_ID + "/input/picture.jpg";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scenes/" + SCENE_ID + "/input/picture.jpg");
            Path outputPath = Paths.get("/root/path/scenes/" + SCENE_ID + "/output/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.load("scenes/" + SCENE_ID + "/output/picture.jpg")).thenReturn(outputPath);

            String result = FileHelper.storeToOutput(SCENE_ID, filepath, null, null);

            verify(storageService).store(resource, "scenes/" + SCENE_ID + "/output/picture.jpg");
            assertEquals(outputPath.toString(), result);
        }

        @Test
        void shouldStoreFileToOutputWithFolderAndPrefix() {
            String filepath = "/root/path/scenes/123/input/picture.jpg";
            String folder = "processed";
            String prefix = "edited";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scenes/123/input/picture.jpg");
            Path outputPath = Paths.get("/root/path/scenes/123/output/processed/edited_picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists("scenes/" + SCENE_ID + "/output/" + folder + "/")).thenReturn(false);
            when(storageService.load("scenes/" + SCENE_ID + "/output/" + folder + "/edited_picture.jpg")).thenReturn(outputPath);

            String result = FileHelper.storeToOutput(SCENE_ID, filepath, folder, prefix);

            verify(storageService).createFolder("scenes/" + SCENE_ID + "/output/" + folder + "/");
            verify(storageService).store(resource, "scenes/" + SCENE_ID + "/output/" + folder + "/edited_picture.jpg");
            assertEquals(outputPath.toString(), result);
        }

        @Test
        void shouldNotCreateFolderIfItAlreadyExists() {
            String filepath = "/root/path/scenes/123/input/picture.jpg";
            String folder = "processed";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scenes/123/input/picture.jpg");
            Path outputPath = Paths.get("/root/path/scenes/123/output/processed/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists("scenes/" + SCENE_ID + "/output/" + folder + "/")).thenReturn(true);
            when(storageService.load("scenes/" + SCENE_ID + "/output/" + folder + "/picture.jpg")).thenReturn(outputPath);

            FileHelper.storeToOutput(SCENE_ID, filepath, folder, null);

            verify(storageService, never()).createFolder(anyString());
        }
    }

    @Nested
    @DisplayName("Store To Temp Tests")
    class StoreToTempTests {

        @Test
        void shouldStoreFileToTemp() {
            String filename = "path/temp-file.jpg";
            Path tempPath = Paths.get("/root/path/tasks/"+ TASK_ID + "/" + NODE_ID + "/" + filename);
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

            when(storageService.folderExists("tasks/" + TASK_ID + "/" + NODE_ID + "/path/")).thenReturn(false);
            when(storageService.load("tasks/" + TASK_ID + "/" + NODE_ID + "/" + filename)).thenReturn(tempPath);

            String result = FileHelper.storeToTemp(TASK_ID, NODE_ID, inputStream, filename);

            verify(storageService).createFolder("tasks/" + TASK_ID + "/" + NODE_ID + "/path/");
            verify(storageService).store(inputStream, "tasks/" + TASK_ID + "/" + NODE_ID + "/" + filename);
            assertEquals(tempPath.toString(), result);
        }

        @Test
        void shouldNotCreateFolderIfItAlreadyExists() {
            String filename = "temp-file.jpg";
            Path tempPath = Paths.get("/root/path/tasks" + TASK_ID + "/" + NODE_ID + "/" + filename);
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

            when(storageService.folderExists("tasks/" + TASK_ID + "/" + NODE_ID + "/")).thenReturn(true);
            when(storageService.load("tasks/" + TASK_ID + "/" + NODE_ID + "/" + filename)).thenReturn(tempPath);

            FileHelper.storeToTemp(TASK_ID, NODE_ID, inputStream, filename);

            verify(storageService, never()).createFolder(anyString());
        }
    }

    @Nested
    @DisplayName("Create Dump Tests")
    class CreateDumpTests {

        @Test
        void shouldCreateDumpFile() {
            String filepath = "/root/path/123/input/picture.jpg";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("123/input/picture.jpg");
            Path dumpPath = Paths.get("/root/path/tasks" + TASK_ID + "/" + NODE_ID + "/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists("tasks/" + TASK_ID + "/" + NODE_ID + "/")).thenReturn(false);
            when(storageService.load("tasks/" + TASK_ID + "/" + NODE_ID + "/picture.jpg")).thenReturn(dumpPath);

            String result = FileHelper.createDump(TASK_ID, NODE_ID, filepath);

            verify(storageService).createFolder("tasks/" + TASK_ID + "/" + NODE_ID + "/");
            verify(storageService).store(resource, "tasks/" + TASK_ID + "/" + NODE_ID + "/picture.jpg");
            assertEquals(dumpPath.toString(), result);
        }

        @Test
        void shouldCreateDumpFromTempFile() {
            String filepath = "upload-image-dir/tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture1.png";
            Path rootPath = Paths.get("upload-image-dir/");
            Path relativePath = Paths.get("tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture1.png");
            Path dumpPath = Paths.get("tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture1.png");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists("tasks/" + TASK_ID + "/" + NODE_ID + "/output/")).thenReturn(false);
            when(storageService.load("tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture1.png")).thenReturn(dumpPath);

            String result = FileHelper.createDump(TASK_ID, NODE_ID, filepath);

            verify(storageService).createFolder("tasks/" + TASK_ID + "/" + NODE_ID + "/output/");
            verify(storageService).store(resource, "tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture1.png");
            assertEquals(dumpPath.toString(), result);
        }

        @Test
        void shouldThrowExceptionWhenResourceIsNull() {
            String filepath = "/root/path/tasks/" + TASK_ID + "/" + NODE_ID + "/input/picture.jpg";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("tasks/" + TASK_ID + "/" + NODE_ID + "/input/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(null);
            when(storageService.folderExists("tasks/" + TASK_ID + "/" + NODE_ID + "/input/")).thenReturn(true);

            assertThrows(StorageException.class, () -> FileHelper.createDump(TASK_ID, NODE_ID, filepath));
        }
    }

    @Test
    void getFullPathShouldCallStorageServiceLoad() {
        String filepath = "scene123/input/picture.jpg";
        Path fullPath = Paths.get("/root/path/123/input/picture.jpg");

        when(storageService.load(filepath)).thenReturn(fullPath);

        String result = FileHelper.getFullPath(filepath);

        verify(storageService).load(filepath);
        assertEquals(fullPath.toString(), result);
    }
}