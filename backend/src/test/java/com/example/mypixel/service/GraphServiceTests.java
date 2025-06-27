package com.example.mypixel.service;

import com.example.mypixel.TestFileUtils;
import com.example.mypixel.config.TestCacheConfig;
import com.example.mypixel.exception.InvalidGraph;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.NodeReference;
import com.example.mypixel.model.node.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import({TestCacheConfig.class})
@ActiveProfiles("test")
public class GraphServiceTests {

    @Autowired
    private NodeCacheService nodeCacheService;

    @Autowired
    private NodeProcessorService nodeProcessorService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private StorageService storageService;

    private final Long sceneId = 1L;

    @BeforeEach
    void setupTestFiles() {
        TestFileUtils.copyResourcesToDirectory(
                "upload-image-dir/scenes/" + sceneId + "/input/",
                "test-images/Picture1.png",
                "test-images/Picture3.png"
        );
    }

    @Test
    void validation_withDuplicates_shouldThrowException() {
        InputNode node1 = new InputNode(0L, "Input", null);
        InputNode node2 = new InputNode(0L, "Input", null);

        OutputNode node3 = new OutputNode(1L, "Output", null);
        OutputNode node4 = new OutputNode(1L, "Output", null);

        List<Long> duplicateIds = List.of(node1.getId(), node3.getId());

        Graph graph = new Graph(List.of(node1, node2, node3, node4));

        Exception exception = assertThrows(InvalidGraph.class,
                () -> graphService.validateGraph(graph));

        String expectedMessage = "Graph contains nodes with duplicate IDs: " + duplicateIds;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void executeGraph_defaultCase_shouldGenerateOutputFiles() {
        graphService.startGraphExecution(getGraph(), sceneId);

        assertTrue(storageService.loadAll("scenes/" +
                        sceneId + "/output").toArray().length > 0);
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/processed/filtered_result_Picture1.png").exists());
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/processed/filtered_result_Picture3.png").exists());}

    private Graph getGraph() {
        List<Node> nodes = new ArrayList<>();

        // Create Input node (id: 1)
        Map<String, Object> inputParams = new HashMap<>();
        List<String> files = new ArrayList<>();
        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture1.png");
        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture3.png");
        inputParams.put("files", files);
        InputNode inputNode = new InputNode(1L, "Input", inputParams);
        nodes.add(inputNode);

        // Create Vector2D node (id: 2)
        Map<String, Object> vectorParams = new HashMap<>();
        vectorParams.put("x", 5);
        vectorParams.put("y", 5);
        Vector2DNode vector2DNode = new Vector2DNode(2L, "Vector2D", vectorParams);
        nodes.add(vector2DNode);

        // Create Floor node (id: 3)
        Map<String, Object> floorParams = new HashMap<>();
        floorParams.put("number", 2.5);
        FloorNode floorNode = new FloorNode(3L, "Floor", floorParams);
        nodes.add(floorNode);

        // Create Blur node (id: 4)
        Map<String, Object> blurParams = new HashMap<>();
        blurParams.put("files", new NodeReference("@node:1:files"));
        blurParams.put("ksize", new NodeReference("@node:2:vector2D"));
        BlurNode blurNode = new BlurNode(4L, "Blur", blurParams);
        nodes.add(blurNode);

        // Create GaussianBlur node (id: 5)
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", new NodeReference("@node:4:files"));
        gaussianParams.put("sizeX", 5);
        gaussianParams.put("sizeY", 5);
        gaussianParams.put("sigmaX", 1.5);
        gaussianParams.put("sigmaY", 1.5);
        GaussianBlurNode gaussianNode = new GaussianBlurNode(5L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);

        // Create BilateralFilter node (id: 6)
        Map<String, Object> bilateralParams = new HashMap<>();
        bilateralParams.put("files", new NodeReference("@node:5:files"));
        bilateralParams.put("d", 9);
        bilateralParams.put("sigmaColor", 75.0);
        bilateralParams.put("sigmaSpace", 75.0);
        BilateralFilterNode bilateralNode = new BilateralFilterNode(6L, "BilateralFilter", bilateralParams);
        nodes.add(bilateralNode);

        // Create String node (id: 7)
        Map<String, Object> stringParams = new HashMap<>();
        stringParams.put("value", "filtered_result");
        StringNode stringNode = new StringNode(7L, "String", stringParams);
        nodes.add(stringNode);

        // Create Output node (id: 8)
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("files", new NodeReference("@node:6:files"));
        outputParams.put("prefix", new NodeReference("@node:7:value"));
        outputParams.put("folder", "processed");
        OutputNode outputNode = new OutputNode(8L, "Output", outputParams);
        nodes.add(outputNode);

        return new Graph(nodes);
    }
}
