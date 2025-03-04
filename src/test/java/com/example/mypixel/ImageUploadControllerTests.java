package com.example.mypixel;

import java.nio.file.Paths;
import java.util.stream.Stream;

import com.example.mypixel.controller.ImageUploadController;
import com.example.mypixel.exception.StorageFileNotFoundException;
import com.example.mypixel.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ImageUploadController.class)
public class ImageUploadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService storageService;

    @BeforeEach
    public void setup() {
        storageService.deleteAll();
        storageService.init();
    }

    @Test
    public void shouldListAllFiles() throws Exception {

        given(storageService.loadAll())
                .willReturn(Stream.of(
                        Paths.get("first.jpg"),
                        Paths.get("second.jpg")
                ));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", containsString("/images/first.jpg")))
                .andExpect(jsonPath("$[1]", containsString("/images/second.jpg")));
    }

    @Test
    public void shouldServeFile() throws Exception {

        String filename = "test.jpg";
        Resource mockResource = mock(Resource.class);
        given(mockResource.getFilename()).willReturn(filename);
        given(mockResource.exists()).willReturn(true);
        given(mockResource.isReadable()).willReturn(true);

        given(storageService.loadAsResource(filename)).willReturn(mockResource);

        mockMvc.perform(get("/images/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"test.jpg\"")))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void shouldHandleFileNotFoundException() throws Exception {

        String filename = "missing.jpg";
        given(storageService.loadAsResource(filename))
                .willThrow(new StorageFileNotFoundException("File not found: " + filename));

        mockMvc.perform(get("/images/{filename}", filename))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUploadFile() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        mockMvc.perform(multipart("/").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You successfully uploaded test.jpg")));

        verify(storageService).store(file);
    }

    @Test
    public void shouldRejectNonImageFile() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes());

        mockMvc.perform(multipart("/").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof IllegalArgumentException));
    }
}