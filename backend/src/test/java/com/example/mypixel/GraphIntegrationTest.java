package com.example.mypixel;


import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.Scene;
import com.example.mypixel.service.SceneService;
import com.example.mypixel.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestcontainersExtension.class)
public class GraphIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SceneService sceneService;

    @Autowired
    private StorageService storageService;

    Long sceneId;

    @BeforeEach
    void setupTestFiles() {
        Scene scene = sceneService.createScene();
        sceneId = scene.getId();

        TestcontainersExtension.uploadTestFileToS3(
                sceneId,
                "test-images/Picture1.png",
                "upload-image-dir/{{scene_id}}/input/Picture1.png"
        );
        TestcontainersExtension.uploadTestFileToS3(
                sceneId,
                "test-images/Picture3.png",
                "upload-image-dir/{{scene_id}}/input/Picture3.png"
        );
    }

    @Test
    void testGraphExecution() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/graph-template-1.json", sceneId, TestcontainersExtension.getLocalstack());

        TestFileUtils.copyResourcesToDirectory(
                "upload-image-dir/" + sceneId + "/input/",
                "test-images/Picture1.png",
                "test-images/Picture3.png"
        );

        ResponseEntity<GraphExecutionTask> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                GraphExecutionTask.class,
                sceneId);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertTrue(TestcontainersExtension.doesObjectExistInS3("output/Picture1.png"));
        assertTrue(TestcontainersExtension.doesObjectExistInS3("output/Picture3.png"));
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/output_1/output1_Picture1.png").exists());
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/output_1/output1_Picture3.png").exists());
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/output_1/upload-image-dir/" + sceneId + "/input/output1_Picture1.png").exists());
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/output_1/upload-image-dir/" + sceneId + "/input/output1_Picture3.png").exists());
    }

    @Test
    void testGraphExecutionWithInvalidGraphJson() {
        String invalidGraphJson = "invalid graph json";

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                invalidGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGraphExecutionWithInvalidSceneId() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/graph-template-1.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                6666666);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testInvalidNodeReference() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/invalid-node-reference.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Should reject invalid node reference");
        assertNotNull(response.getBody(), "Should return error details");
        assertTrue(response.getBody().contains("999"),
                "Error should mention invalid node ID");
    }
}