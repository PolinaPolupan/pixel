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

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

        TestFileUtils.copyResourcesToDirectory(
                "upload-image-dir/" + sceneId + "/input/",
                "test-images/Picture1.png",
                "test-images/Picture3.png"
        );
    }

    @Test
    void testGraphExecution() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/graph-template-1.json", sceneId, TestcontainersExtension.getLocalstack());

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

    @Test
    void testInvalidNodeType() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/invalid-node-type.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Should return error details");
    }

    @Test
    void testInvalidNodeProperty() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/invalid-node-property.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Should return error details");
    }

    @Test
    void testInvalidAwsCredentials() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/invalid-aws.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Should return error details");
    }

    @Test
    void testMissingRequiredInputs() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/missing-required-inputs.json", sceneId, TestcontainersExtension.getLocalstack());


        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Graph with missing required inputs should be rejected");
    }

    @Test
    void testConcurrentLoad() {
        int concurrentUsers = 10;
        int requestsPerUser = 5;

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);

        List<CompletableFuture<Long>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int user = 0; user < concurrentUsers; user++) {
            for (int req = 0; req < requestsPerUser; req++) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    long requestStart = System.currentTimeMillis();

                    String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                            "test-json/graph-template-1.json", sceneId, TestcontainersExtension.getLocalstack());

                    restTemplate.postForEntity(
                            "/v1/scene/{sceneId}/graph",
                            testGraphJson,
                            String.class,
                            sceneId);

                    return System.currentTimeMillis() - requestStart;
                }, executor));
            }
        }

        List<Long> executionTimes = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;

        DoubleSummaryStatistics stats = executionTimes.stream()
                .mapToDouble(Long::doubleValue)
                .summaryStatistics();

        double throughput = (concurrentUsers * requestsPerUser * 1000.0) / totalTime;

        System.out.println("\n=== Load Test Results ===");
        System.out.println("Concurrent users: " + concurrentUsers);
        System.out.println("Requests per user: " + requestsPerUser);
        System.out.println("Total requests: " + (concurrentUsers * requestsPerUser));
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        System.out.println("Average response time: " + String.format("%.2f", stats.getAverage()) + "ms");
        System.out.println("Min response time: " + stats.getMin() + "ms");
        System.out.println("Max response time: " + stats.getMax() + "ms");

        executor.shutdown();
    }
}