package com.example.mypixel.controller;

import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.example.mypixel.exception.InvalidImageFormat;
import com.example.mypixel.service.TempStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageUploadController.class)
public class ImageUploadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileManager fileManager;

    private final String sceneId = "scene123";
    private final String baseRoute = "/v1/scene/" + sceneId + "/image/";

    private TempStorageService service;

    @TempDir
    File tempDir;

    @BeforeEach
    public void init() {
        service = new TempStorageService(tempDir.getAbsolutePath());
        service.init();
        service.createFolder(sceneId);
    }

    @Test
    public void shouldListAllImages() throws Exception {
        given(fileManager.loadAll(sceneId))
                .willReturn(Stream.of(
                        Paths.get("first.jpg"),
                        Paths.get("second.jpg")
                ));

        mockMvc.perform(get(baseRoute))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", containsString("/first.jpg")))
                .andExpect(jsonPath("$[1]", containsString("/second.jpg")));
    }

    @Test
    public void shouldServeImage() throws Exception {
        String filename = "test.jpg";
        Resource mockResource = mock(Resource.class);
        given(mockResource.getFilename()).willReturn(filename);
        given(mockResource.getFile()).willReturn(new File(filename));
        given(mockResource.exists()).willReturn(true);
        given(mockResource.isReadable()).willReturn(true);

        given(fileManager.loadAsResource(filename, sceneId)).willReturn(mockResource);

        mockMvc.perform(get(baseRoute + "{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"test.jpg\"")))
                .andExpect(header().string("Content-Type", containsString("image/jpeg")))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void shouldHandleFileNotFoundException() throws Exception {
        String filename = "missing.jpg";
        given(fileManager.loadAsResource(filename, sceneId))
                .willReturn(null);

        mockMvc.perform(get(baseRoute + "{filename}", filename))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUploadImages() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 1".getBytes());

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 2".getBytes());

        mockMvc.perform(multipart(baseRoute).file(file1).file(file2))
                .andExpect(status().isCreated());

        verify(fileManager).store(file1, sceneId);
        verify(fileManager).store(file2, sceneId);
    }

    @Test
    public void shouldRejectNonImageFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes());

        mockMvc.perform(multipart(baseRoute).file(file))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(InvalidImageFormat.class, result.getResolvedException()));
    }

    @Test
    public void shouldAcceptPngImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "test png content".getBytes());

        mockMvc.perform(multipart(baseRoute).file(file))
                .andExpect(status().isCreated());

        verify(fileManager).store(file, sceneId);
    }
}