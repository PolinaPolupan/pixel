package com.example.mypixel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.mypixel.exception.StorageException;
import com.example.mypixel.model.node.Node;
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

    private FileHelper fileHelper;

    @Mock
    private StorageService storageService;

    @Mock
    private Node node;

    @Mock
    private Resource resource;

    private static final String SCENE_ID = "scene123";
    private static final Long NODE_ID = 456L;

    @BeforeEach
    void setUp() {
        when(node.getSceneId()).thenReturn(SCENE_ID);
        fileHelper = new FileHelper(storageService, node);
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
            assertEquals(expected, fileHelper.extractFilename(input));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldHandleNullAndEmptyInput(String input) {
            assertEquals("", fileHelper.extractFilename(input));
        }

        @Test
        void shouldHandlePathEndingWithSlash() {
            assertEquals("", fileHelper.extractFilename("/path/to/directory/"));
        }
    }

    @Nested
    @DisplayName("Extract Path Tests")
    class ExtractPathTests {

        @Test
        void shouldExtractPath() {
            String filepath = "scene123/input/pic/Picture.jpeg";
            assertEquals("scene123/input/pic/", fileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractEmptyPath() {
            String filepath = "Picture.jpeg";
            assertEquals("", fileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractPathAfterInput() {
            String filepath = "scene123/input/pic/Picture.jpeg";
            assertEquals("pic/", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldExtractPathAfterTemp() {
            String filepath = "scene123/temp/456/path/Picture.jpeg";
            assertEquals("path/", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleMultipleSubfoldersAfterInput() {
            String filepath = "scene123/input/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleMultipleSubfoldersAfterTemp() {
            String filepath = "scene123/temp/nodeId/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldReturnEmptyWhenNoInputInPath() {
            String filepath = "scene123/output/pic/Picture.jpeg";
            assertEquals("", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleInputAsLastSegment() {
            String filepath = "scene123/folder/input";
            assertEquals("", fileHelper.extractRelativeWorkspacePath(filepath));
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
            assertEquals(expected, fileHelper.addPrefixToFilename(filename, prefix));
        }
    }

    @Nested
    @DisplayName("Store To Output Tests")
    class StoreToOutputTests {

        @Test
        void shouldStoreFileToOutputWithoutFolderOrPrefix() {
            String filepath = "/root/path/scene123/input/picture.jpg";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scene123/input/picture.jpg");
            Path outputPath = Paths.get("/root/path/scene123/output/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.load(SCENE_ID + "/output/picture.jpg")).thenReturn(outputPath);

            String result = fileHelper.storeToOutput(filepath, null, null);

            verify(storageService).store(resource, SCENE_ID + "/output/picture.jpg");
            assertEquals(outputPath.toString(), result);
        }

        @Test
        void shouldStoreFileToOutputWithFolderAndPrefix() {
            String filepath = "/root/path/scene123/input/picture.jpg";
            String folder = "processed";
            String prefix = "edited";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scene123/input/picture.jpg");
            Path outputPath = Paths.get("/root/path/scene123/output/processed/edited_picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists(SCENE_ID + "/output/" + folder + "/")).thenReturn(false);
            when(storageService.load(SCENE_ID + "/output/" + folder + "/edited_picture.jpg")).thenReturn(outputPath);

            String result = fileHelper.storeToOutput(filepath, folder, prefix);

            verify(storageService).createFolder(SCENE_ID + "/output/" + folder + "/");
            verify(storageService).store(resource, SCENE_ID + "/output/" + folder + "/edited_picture.jpg");
            assertEquals(outputPath.toString(), result);
        }

        @Test
        void shouldNotCreateFolderIfItAlreadyExists() {
            String filepath = "/root/path/scene123/input/picture.jpg";
            String folder = "processed";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scene123/input/picture.jpg");
            Path outputPath = Paths.get("/root/path/scene123/output/processed/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists(SCENE_ID + "/output/" + folder + "/")).thenReturn(true);
            when(storageService.load(SCENE_ID + "/output/" + folder + "/picture.jpg")).thenReturn(outputPath);

            fileHelper.storeToOutput(filepath, folder, null);

            verify(storageService, never()).createFolder(anyString());
        }
    }

    @Nested
    @DisplayName("Store To Temp Tests")
    class StoreToTempTests {

        @Test
        void shouldStoreFileToTemp() {
            when(node.getId()).thenReturn(NODE_ID);
            String filename = "path/temp-file.jpg";
            Path tempPath = Paths.get("/root/path/scene123/temp/" + NODE_ID + "/" + filename);
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

            when(storageService.folderExists(SCENE_ID + "/temp/" + NODE_ID + "/path/")).thenReturn(false);
            when(storageService.load(SCENE_ID + "/temp/" + NODE_ID + "/" + filename)).thenReturn(tempPath);

            String result = fileHelper.storeToTemp(inputStream, filename);

            verify(storageService).createFolder(SCENE_ID + "/temp/" + NODE_ID + "/path/");
            verify(storageService).store(inputStream, SCENE_ID + "/temp/" + NODE_ID + "/" + filename);
            assertEquals(tempPath.toString(), result);
        }

        @Test
        void shouldNotCreateFolderIfItAlreadyExists() {
            when(node.getId()).thenReturn(NODE_ID);
            String filename = "temp-file.jpg";
            Path tempPath = Paths.get("/root/path/scene123/temp/" + NODE_ID + "/" + filename);
            InputStream inputStream = new ByteArrayInputStream("test data".getBytes());

            when(storageService.folderExists(SCENE_ID + "/temp/" + NODE_ID + "/")).thenReturn(true);
            when(storageService.load(SCENE_ID + "/temp/" + NODE_ID + "/" + filename)).thenReturn(tempPath);

            fileHelper.storeToTemp(inputStream, filename);

            verify(storageService, never()).createFolder(anyString());
        }
    }

    @Nested
    @DisplayName("Create Dump Tests")
    class CreateDumpTests {

        @Test
        void shouldCreateDumpFile() {
            when(node.getId()).thenReturn(NODE_ID);
            String filepath = "/root/path/scene123/input/picture.jpg";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scene123/input/picture.jpg");
            Path dumpPath = Paths.get("/root/path/scene123/temp/" + NODE_ID + "/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists(SCENE_ID + "/temp/" + NODE_ID + "/")).thenReturn(false);
            when(storageService.load(SCENE_ID + "/temp/" + NODE_ID + "/picture.jpg")).thenReturn(dumpPath);

            String result = fileHelper.createDump(filepath);

            verify(storageService).createFolder(SCENE_ID + "/temp/" + NODE_ID + "/");
            verify(storageService).store(resource, SCENE_ID + "/temp/" + NODE_ID + "/picture.jpg");
            assertEquals(dumpPath.toString(), result);
        }

        @Test
        void shouldCreateDumpFromTempFile() {
            when(node.getId()).thenReturn(NODE_ID);
            String filepath = "upload-image-dir/" + SCENE_ID + "/temp/123/output/Picture1.png";
            Path rootPath = Paths.get("upload-image-dir/" + SCENE_ID);
            Path relativePath = Paths.get("temp/123/output/Picture1.png");
            Path dumpPath = Paths.get(SCENE_ID + "/temp/" + NODE_ID + "/output/Picture1.png");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(resource);
            when(storageService.folderExists(SCENE_ID + "/temp/" + NODE_ID + "/output/")).thenReturn(false);
            when(storageService.load(SCENE_ID + "/temp/" + NODE_ID + "/output/Picture1.png")).thenReturn(dumpPath);

            String result = fileHelper.createDump(filepath);

            verify(storageService).createFolder(SCENE_ID + "/temp/" + NODE_ID + "/output/");
            verify(storageService).store(resource, SCENE_ID + "/temp/" + NODE_ID + "/output/Picture1.png");
            assertEquals(dumpPath.toString(), result);
        }

        @Test
        void shouldThrowExceptionWhenResourceIsNull() {
            when(node.getId()).thenReturn(NODE_ID);
            String filepath = "/root/path/scene123/input/picture.jpg";
            Path rootPath = Paths.get("/root/path");
            Path relativePath = Paths.get("scene123/input/picture.jpg");

            when(storageService.getRootLocation()).thenReturn(rootPath);
            when(storageService.loadAsResource(relativePath.toString())).thenReturn(null);
            when(storageService.folderExists(SCENE_ID + "/temp/" + NODE_ID + "/")).thenReturn(true);

            assertThrows(StorageException.class, () -> fileHelper.createDump(filepath));
        }
    }

    @Test
    void getFullPathShouldCallStorageServiceLoad() {
        String filepath = "scene123/input/picture.jpg";
        Path fullPath = Paths.get("/root/path/scene123/input/picture.jpg");

        when(storageService.load(filepath)).thenReturn(fullPath);

        String result = fileHelper.getFullPath(filepath);

        verify(storageService).load(filepath);
        assertEquals(fullPath.toString(), result);
    }
}