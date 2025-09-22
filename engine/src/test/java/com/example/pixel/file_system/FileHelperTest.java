package com.example.pixel.file_system;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.pixel.config.TestCacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@SpringBootTest
@Import({TestCacheConfig.class})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@Tag("integration")
public class FileHelperTest {

    @MockitoBean
    private StorageService storageService;

    @InjectMocks
    @Autowired
    private FileHelper fileHelper;

    private static final Long SCENE_ID = 123L;
    private static final Long TASK_ID = 123L;
    private static final Long NODE_ID = 456L;

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
            String filepath = "123/input/pic/Picture.jpeg";
            assertEquals("123/input/pic/", fileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractEmptyPath() {
            String filepath = "Picture.jpeg";
            assertEquals("", fileHelper.extractPath(filepath));
        }

        @Test
        void shouldExtractPathAfterInput() {
            String filepath = "scenes/" + SCENE_ID + "/input/pic/Picture.jpeg";
            assertEquals("pic/", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldExtractPathAfterId() {
            String filepath = "tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture.jpeg";
            assertEquals("output/", fileHelper.extractRelativeWorkspacePath(filepath));
        }


        @Test
        void shouldHandleMultipleSubfoldersAfterInput() {
            String filepath = "scenes/" + SCENE_ID + "/input/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleMultipleSubfoldersAfter() {
            String filepath = "tasks/" + TASK_ID + "/" + NODE_ID + "/folder1/folder2/Picture.jpeg";
            assertEquals("folder1/folder2/", fileHelper.extractRelativeWorkspacePath(filepath));
        }

        @Test
        void shouldHandleInputAsLastSegment() {
            String filepath = "scenes" + SCENE_ID + "/input";
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
            String filepath = "scenes/" + SCENE_ID + "/input/picture.jpg";
            String outputPath = "scenes/" + SCENE_ID + "/picture.jpg";

            String result = fileHelper.storeFromWorkspaceToScene(filepath, null, null);

            verify(storageService).store(filepath, outputPath);
            assertEquals(outputPath, result);
        }

        @Test
        void shouldStoreFileToOutputWithFolderAndPrefix() {
            String filepath = "scenes/" + SCENE_ID + "/input/picture.jpg";
            String folder = "processed";
            String prefix = "edited";
            String outputPath = "scenes/" + SCENE_ID + "/processed/edited_picture.jpg";

            String result = fileHelper.storeFromWorkspaceToScene(filepath, folder, prefix);

            verify(storageService).store(filepath, outputPath);
            assertEquals(outputPath, result);
        }
    }

    @Nested
    @DisplayName("Create Dump Tests")
    class CreateDumpTests {

        @Test
        void shouldCreateDumpFile() {
            String filepath = SCENE_ID + "/input/picture.jpg";
            String dumpPath = "tasks/" + TASK_ID + "/" + NODE_ID + "/picture.jpg";

            String result = fileHelper.storeFromWorkspaceToTask(TASK_ID, NODE_ID, filepath);

            verify(storageService).store(filepath, dumpPath);
            assertEquals(dumpPath, result);
        }

        @Test
        void shouldCreateDumpFromTempFile() {
            String filepath = "tasks/" + "78" + "/" + "768" + "/output/Picture1.png";
            String dumpPath = "tasks/" + TASK_ID + "/" + NODE_ID + "/output/Picture1.png";

            String result = fileHelper.storeFromWorkspaceToTask(TASK_ID, NODE_ID, filepath);

            verify(storageService).store(filepath, dumpPath);
            assertEquals(dumpPath, result);
        }

        @Test
        void shouldThrowExceptionWhenResourceIsNull() {
            String filepath = "/root/path/tasks/" + TASK_ID + "/" + NODE_ID + "/input/picture.jpg";

            doThrow(RuntimeException.class).when(storageService).store(anyString(), anyString());

            assertThrows(RuntimeException.class, () -> fileHelper.storeFromWorkspaceToTask(TASK_ID, NODE_ID, filepath));
        }
    }
}