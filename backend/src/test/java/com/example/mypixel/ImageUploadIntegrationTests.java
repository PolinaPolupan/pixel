package com.example.mypixel;


import com.example.mypixel.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageUploadIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FileManager fileManager;

    private final String sceneId = "scene123";
    private final String baseRoute = "/v1/scene/" + sceneId + "/image/";

    @MockitoBean
    private StorageService storageService;

    @BeforeEach
    public void init() {
        fileManager.createScene(sceneId);
    }

    @Test
    public void shouldUploadImage() {
        ClassPathResource resource = new ClassPathResource("/testupload.jpg", getClass());

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        ResponseEntity<String> response = restTemplate.postForEntity(baseRoute, map, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        then(storageService).should().store(any(MultipartFile.class), any(String.class));
    }

    @Test
    public void shouldDownloadImage() throws IOException {
        ClassPathResource resource = new ClassPathResource("/testupload.jpg", getClass());

        given(storageService.loadAsResource(sceneId+"/testupload.jpg")).willReturn(resource);
        byte[] expectedImageContent = StreamUtils.copyToByteArray(resource.getInputStream());

        ResponseEntity<byte[]> response = restTemplate
                .getForEntity(baseRoute + "{filename}", byte[].class, "testupload.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"testupload.jpg\"");
        assertThat(response.getBody()).isEqualTo(expectedImageContent);
    }
}