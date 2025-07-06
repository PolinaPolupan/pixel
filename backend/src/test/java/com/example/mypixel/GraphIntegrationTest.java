package com.example.mypixel;


import com.example.mypixel.model.GraphExecutionTask;
import com.example.mypixel.model.Scene;
import com.example.mypixel.service.SceneService;
import com.example.mypixel.service.StorageService;
import com.example.mypixel.util.TestFileUtils;
import com.example.mypixel.util.TestJsonTemplates;
import com.example.mypixel.util.TestcontainersExtension;
import lombok.extern.slf4j.Slf4j;
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


import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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
                "upload-image-dir/scenes/" + sceneId + "/input/",
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
    void testInvalidIdLongRange() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/invalid-id-long-range.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testInvalidIdString() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/invalid-id-string.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testIntegerOverflow() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/integer-overflow.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDoubleOverflow() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/double-overflow.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/graph",
                testGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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

                    ResponseEntity<GraphExecutionTask> response = restTemplate.postForEntity(
                            "/v1/scene/{sceneId}/graph",
                            testGraphJson,
                            GraphExecutionTask.class,
                            sceneId);

                    assertEquals(HttpStatus.OK, response.getStatusCode());

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

        log.info("\n=== Load Test Results ===");
        log.info("Concurrent users: {}", concurrentUsers);
        log.info("Requests per user: {}", requestsPerUser);
        log.info("Total requests: {}", concurrentUsers * requestsPerUser);
        log.info("Total time: {}ms", totalTime);
        log.info("Throughput: {} requests/second", String.format("%.2f", throughput));
        log.info("Average response time: {}ms", String.format("%.2f", stats.getAverage()));
        log.info("Min response time: {}ms", stats.getMin());
        log.info("Max response time: {}ms", stats.getMax());

        executor.shutdown();
    }
}