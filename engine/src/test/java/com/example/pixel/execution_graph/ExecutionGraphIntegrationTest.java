package com.example.pixel.execution_graph;

import com.example.pixel.execution_graph.model.ExecutionGraph;
import com.example.pixel.execution_graph.model.ExecutionGraphPayload;
import com.example.pixel.execution_graph.service.GraphService;
import com.example.pixel.execution_task.model.ExecutionTaskPayload;
import com.example.pixel.file_system.service.StorageService;
import com.example.pixel.util.TestFileUtils;
import com.example.pixel.util.TestJsonTemplates;
import com.example.pixel.util.TestcontainersExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import static com.example.pixel.util.TestcontainersExtension.localstack;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestcontainersExtension.class)
@Tag("integration")
public class ExecutionGraphIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GraphService graphService;

    @Autowired
    private StorageService storageService;

    private Long sceneId;

    @BeforeEach
    void setupTestFiles() {
        ExecutionGraphPayload scene = graphService.createExecutionGraph();
        sceneId = scene.getId();

        TestcontainersExtension.uploadTestFileToS3(
                sceneId,
                "test-images/Picture1.png",
                "Picture1.png"
        );
        TestcontainersExtension.uploadTestFileToS3(
                sceneId,
                "test-images/Picture3.png",
                "Picture3.png"
        );

        TestFileUtils.copyResourcesToDirectory(
                "upload-image-dir/scenes/" + sceneId + "/input/",
                "test-images/Picture1.png",
                "test-images/Picture3.png"
        );
    }

    @Test
    void testEndpointConfiguration() {
        log.info("=== Testing Endpoint Configuration ===");

        String externalEndpoint = localstack.getEndpointOverride(S3).toString();
        String containerName = localstack.getContainerName();
        if (containerName.startsWith("/")) {
            containerName = containerName.substring(1);
        }
        String internalEndpoint = "http://" + containerName + ":4566";

        log.info("External endpoint (for Java/host): {}", externalEndpoint);
        log.info("Internal endpoint (for Python/container): {}", internalEndpoint);
        log.info("Container name: {}", localstack.getContainerName());

        // Test that both endpoints have the right format
        assertTrue(externalEndpoint.contains("127.0.0.1") || externalEndpoint.contains("localhost"));
        assertFalse(internalEndpoint.contains("127.0.0.1"));
        assertFalse(internalEndpoint.contains("localhost"));
        assertTrue(internalEndpoint.endsWith(":4566"));
    }

    @Test
    public void testNodeServiceConnectivity() {
        String nodeServiceUrl = System.getProperty("node.service.url");

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(nodeServiceUrl + "info", Map.class);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().containsKey("registered_nodes"));
        } catch (Exception e) {
            fail("Failed to connect to node service: " + e.getMessage());
        }
    }

    @Test
    void testGraphExecution() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/graph-template-1.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<ExecutionTaskPayload> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
                ExecutionTaskPayload.class,
                sceneId);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    try {
                        return TestcontainersExtension.doesObjectExistInS3("Picture1.png")
                                && TestcontainersExtension.doesObjectExistInS3("Picture3.png")
                                && storageService.loadAsResource("scenes/" + sceneId + "/output_1/output1_Picture1.png").exists()
                                && storageService.loadAsResource("scenes/" + sceneId + "/output_1/output1_Picture3.png").exists();
                    } catch (Exception e) {
                        return false;
                    }
                });

        assertTrue(TestcontainersExtension.doesObjectExistInS3("Picture1.png"));
        assertTrue(TestcontainersExtension.doesObjectExistInS3("Picture3.png"));
        assertTrue(storageService.loadAsResource("scenes/" + sceneId + "/output_1/output1_Picture1.png").exists());
        assertTrue(storageService.loadAsResource("scenes/" + sceneId + "/output_1/output1_Picture3.png").exists());
    }

    @Test
    void testGraphExecutionWithInvalidSceneId() {
        String testGraphJson = TestJsonTemplates.getGraphJsonWithTestCredentials(
                "test-json/graph-template-1.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testGraphJson,
                String.class,
                6666666);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testInvalidNodeType() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/invalid-node-type.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Should return error details");
    }

    @Test
    void testInvalidAwsCredentials() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/invalid-aws.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody(), "Should return error details");
    }

    @Test
    void testMissingRequiredInputs() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/missing-required-inputs.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Graph with missing required inputs should be rejected");
    }

    @Test
    void testInvalidIdLongRange() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/invalid-id-long-range.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testInvalidIdString() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/invalid-id-string.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testIntegerOverflow() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/integer-overflow.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
                String.class,
                sceneId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDoubleOverflow() {
        ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                "test-json/double-overflow.json", sceneId, TestcontainersExtension.getLocalstack());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/scene/{sceneId}/exec",
                testExecutionGraphJson,
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

                     ExecutionGraph testExecutionGraphJson = TestJsonTemplates.loadGraph(
                            "test-json/graph-template-1.json", sceneId, TestcontainersExtension.getLocalstack());

                    ResponseEntity<ExecutionTaskPayload> response = restTemplate.postForEntity(
                            "/v1/scene/{sceneId}/exec",
                            testExecutionGraphJson,
                            ExecutionTaskPayload.class,
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