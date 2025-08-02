package com.example.mypixel.model;

import com.example.mypixel.exception.InvalidGraph;
import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.util.TestGraphFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class GraphTests {

    private final Long sceneId = 1L;

    @Test
    void  constructValidGraph_shouldSucceed() {
        Graph graph = TestGraphFactory.getDefaultGraph(sceneId);

        assertNotNull(graph);
        assertEquals(8, graph.getNodes().size());
        assertEquals(8, graph.getTopologicalOrder().size());
    }

    @Test
    void constructGraphWithDuplicateIds_shouldThrowInvalidGraphException() {
        List<Node> nodes = new ArrayList<>();

        Map<String, Object> floorParams = new HashMap<>();
        floorParams.put("input", 56);
        FloorNode floorNode1 = new FloorNode(4L, "Floor", floorParams);
        nodes.add(floorNode1);

        FloorNode floorNode2 = new FloorNode(4L, "Floor", floorParams);
        nodes.add(floorNode2);

        InvalidGraph exception = assertThrows(InvalidGraph.class, () -> new Graph(nodes));
        assertTrue(exception.getMessage().contains("duplicate IDs"));
        assertTrue(exception.getMessage().contains("4"));
    }

    @Test
    void nodeOutputs_shouldBeCorrectlyIdentified() {
        // Create a graph with the template nodes
        Graph graph = TestGraphFactory.getDefaultGraph(sceneId);
        List<Node> nodes = graph.getNodes();

        // Get the node outputs map
        Map<Node, List<Node>> nodeOutputs = graph.getNodeOutputs();

        // Reference nodes by their index in the list for clarity
        Node inputNode = nodes.get(0);      // Input node (id: 1)
        Node vectorNode = nodes.get(1);     // Vector2D node (id: 2)
        Node floorNode = nodes.get(2);      // Floor node (id: 3)
        Node blurNode = nodes.get(3);       // Blur node (id: 4)
        Node gaussianNode = nodes.get(4);   // GaussianBlur node (id: 5)
        Node bilateralNode = nodes.get(5);  // BilateralFilter node (id: 6)
        Node stringNode = nodes.get(6);     // String node (id: 7)
        Node outputNode = nodes.get(7);     // Output node (id: 8)

        // Verify Input node (1) outputs: should connect to Blur node (4)
        assertEquals(1, nodeOutputs.get(inputNode).size(), "Input node should have 1 output");
        assertTrue(nodeOutputs.get(inputNode).contains(blurNode), "Input node should connect to Blur node");

        // Verify Vector2D node (2) outputs: should connect to Blur node (4)
        assertEquals(1, nodeOutputs.get(vectorNode).size(), "Vector2D node should have 1 output");
        assertTrue(nodeOutputs.get(vectorNode).contains(blurNode), "Vector2D node should connect to Blur node");

        // Verify Floor node (3) outputs: should have no connections
        assertTrue(nodeOutputs.get(floorNode).isEmpty(), "Floor node should have no outputs");

        // Verify Blur node (4) outputs: should connect to GaussianBlur node (5)
        assertEquals(1, nodeOutputs.get(blurNode).size(), "Blur node should have 1 output");
        assertTrue(nodeOutputs.get(blurNode).contains(gaussianNode), "Blur node should connect to GaussianBlur node");

        // Verify GaussianBlur node (5) outputs: should connect to BilateralFilter node (6)
        assertEquals(1, nodeOutputs.get(gaussianNode).size(), "GaussianBlur node should have 1 output");
        assertTrue(nodeOutputs.get(gaussianNode).contains(bilateralNode), "GaussianBlur node should connect to BilateralFilter node");

        // Verify BilateralFilter node (6) outputs: should connect to Output node (8)
        assertEquals(1, nodeOutputs.get(bilateralNode).size(), "BilateralFilter node should have 1 output");
        assertTrue(nodeOutputs.get(bilateralNode).contains(outputNode), "BilateralFilter node should connect to Output node");

        // Verify String node (7) outputs: should connect to Output node (8)
        assertEquals(1, nodeOutputs.get(stringNode).size(), "String node should have 1 output");
        assertTrue(nodeOutputs.get(stringNode).contains(outputNode), "String node should connect to Output node");

        // Verify Output node (8) outputs: should have no connections
        assertTrue(nodeOutputs.get(outputNode).isEmpty(), "Output node should have no outputs");
    }

    @Test
    void topologicalSort_shouldProduceCorrectOrdering() {
        Graph graph = TestGraphFactory.getDefaultGraph(sceneId);
        List<Node> nodes = graph.getNodes();

        // Get the topological ordering
        List<Node> topologicalOrder = graph.getTopologicalOrder();

        // Verify the size matches the number of nodes
        assertEquals(8, topologicalOrder.size(), "Topological order should contain all nodes");

        // Get references to all nodes by their index for readability
        Node inputNode = nodes.get(0);      // Input node (id: 1)
        Node vectorNode = nodes.get(1);     // Vector2D node (id: 2)
        Node floorNode = nodes.get(2);      // Floor node (id: 3)
        Node blurNode = nodes.get(3);       // Blur node (id: 4)
        Node gaussianNode = nodes.get(4);   // GaussianBlur node (id: 5)
        Node bilateralNode = nodes.get(5);  // BilateralFilter node (id: 6)
        Node stringNode = nodes.get(6);     // String node (id: 7)
        Node outputNode = nodes.get(7);     // Output node (id: 8)

        // Find positions of each node in the topological order
        int posInput = topologicalOrder.indexOf(inputNode);
        int posVector = topologicalOrder.indexOf(vectorNode);
        int posFloor = topologicalOrder.indexOf(floorNode);
        int posBlur = topologicalOrder.indexOf(blurNode);
        int posGaussian = topologicalOrder.indexOf(gaussianNode);
        int posBilateral = topologicalOrder.indexOf(bilateralNode);
        int posString = topologicalOrder.indexOf(stringNode);
        int posOutput = topologicalOrder.indexOf(outputNode);

        // Verify all nodes are present in the order
        assertNotEquals(-1, posInput, "Input node missing from topological order");
        assertNotEquals(-1, posVector, "Vector2D node missing from topological order");
        assertNotEquals(-1, posFloor, "Floor node missing from topological order");
        assertNotEquals(-1, posBlur, "Blur node missing from topological order");
        assertNotEquals(-1, posGaussian, "GaussianBlur node missing from topological order");
        assertNotEquals(-1, posBilateral, "BilateralFilter node missing from topological order");
        assertNotEquals(-1, posString, "String node missing from topological order");
        assertNotEquals(-1, posOutput, "Output node missing from topological order");

        // Verify dependency relationships:

        // Input node must come before Blur node
        assertTrue(posInput < posBlur,
                "Input node (1) must come before Blur node (4) in topological order");

        // Vector2D node must come before Blur node
        assertTrue(posVector < posBlur,
                "Vector2D node (2) must come before Blur node (4) in topological order");

        // Blur node must come before GaussianBlur node
        assertTrue(posBlur < posGaussian,
                "Blur node (4) must come before GaussianBlur node (5) in topological order");

        // GaussianBlur node must come before BilateralFilter node
        assertTrue(posGaussian < posBilateral,
                "GaussianBlur node (5) must come before BilateralFilter node (6) in topological order");

        // BilateralFilter node must come before Output node
        assertTrue(posBilateral < posOutput,
                "BilateralFilter node (6) must come before Output node (8) in topological order");

        // String node must come before Output node
        assertTrue(posString < posOutput,
                "String node (7) must come before Output node (8) in topological order");

        // The FloorNode has no connections, so it can be anywhere in the ordering
        // But we can still verify it's in the ordering
        assertTrue(posFloor >= 0, "Floor node (3) must be in the topological order");

        // Additionally, verify the main processing chain order is correct:
        // Input -> Blur -> GaussianBlur -> BilateralFilter -> Output
        assertTrue(posInput < posBlur && posBlur < posGaussian &&
                        posGaussian < posBilateral && posBilateral < posOutput,
                "Main processing chain ordering is incorrect");
    }

    @Test
    void graphWithCycle_shouldThrowInvalidGraphException() {
        List<Node> nodes = new ArrayList<>();

        // Create Input node (id: 10)
        Map<String, Object> inputParams = new HashMap<>();
        List<String> files = new ArrayList<>();
        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture1.png");
        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture3.png");
        inputParams.put("input", files);
        InputNode inputNode = new InputNode(10L, "Input", inputParams);
        nodes.add(inputNode);

        // Create Floor node (id: 4)
        Map<String, Object> floorParams = new HashMap<>();
        floorParams.put("input", 56);
        FloorNode floorNode = new FloorNode(4L, "Floor", floorParams);
        nodes.add(floorNode);

        // Create GaussianBlur node (id: 1)
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", new NodeReference("@node:10:output"));
        gaussianParams.put("sizeX", 33);
        gaussianParams.put("sizeY", 33);
        gaussianParams.put("sigmaX", new NodeReference("@node:4:output"));
        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
        GaussianBlurNode gaussianNode = new GaussianBlurNode(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);

        // Create GaussianBlur node (id: 2)
        Map<String, Object> gaussianParams2 = new HashMap<>();
        gaussianParams2.put("files", new NodeReference("@node:3:files"));
        gaussianParams2.put("sizeX", 33);
        gaussianParams2.put("sizeY", 33);
        gaussianParams2.put("sigmaX", new NodeReference("@node:4:output"));
        gaussianParams2.put("sigmaY", 1.5); // Not in JSON, using default value
        GaussianBlurNode gaussianNode2 = new GaussianBlurNode(2L, "GaussianBlur", gaussianParams2);
        nodes.add(gaussianNode2);

        // Create GaussianBlur node (id: 3)
        Map<String, Object> gaussianParams3 = new HashMap<>();
        gaussianParams3.put("files", new NodeReference("@node:2:files"));
        gaussianParams3.put("sizeX", 33);
        gaussianParams3.put("sizeY", 33);
        gaussianParams3.put("sigmaX", new NodeReference("@node:4:output"));
        gaussianParams3.put("sigmaY", 1.5); // Not in JSON, using default value
        GaussianBlurNode gaussianNode3 = new GaussianBlurNode(3L, "GaussianBlur", gaussianParams3);
        nodes.add(gaussianNode3);

        InvalidGraph exception = assertThrows(InvalidGraph.class, () -> new Graph(nodes));
        assertTrue(exception.getMessage().contains("cycle"));
    }

    @Test
    void graphWithSelfReference_shouldThrowInvalidGraphException() {
        List<Node> nodes = new ArrayList<>();

        // Create GaussianBlur node (id: 1)
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", new NodeReference("@node:1:files"));
        gaussianParams.put("sizeX", 33);
        gaussianParams.put("sizeY", 33);
        gaussianParams.put("sigmaX", 5);
        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
        GaussianBlurNode gaussianNode = new GaussianBlurNode(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);

        InvalidGraph exception = assertThrows(InvalidGraph.class, () -> new Graph(nodes));
        assertTrue(exception.getMessage().contains("cycle"));
    }

    @Test
    void graphWithInvalidReferenceId_shouldThrowInvalidGraphException() {
        List<Node> nodes = new ArrayList<>();

        // Create GaussianBlur node (id: 1)
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", new NodeReference("@node:10:files"));
        gaussianParams.put("sizeX", 33);
        gaussianParams.put("sizeY", 33);
        gaussianParams.put("sigmaX", 5);
        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
        GaussianBlurNode gaussianNode = new GaussianBlurNode(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);

        InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () -> new Graph(nodes));
        assertTrue(exception.getMessage().contains("Invalid node reference: Node with id 10 is not found"));
    }

    @Test
    void graphWithInvalidReferenceKey_shouldThrowInvalidGraphException() {
        List<Node> nodes = new ArrayList<>();

        // Create Input node (id: 10)
        Map<String, Object> inputParams = new HashMap<>();
        List<String> files = new ArrayList<>();
        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture1.png");
        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture3.png");
        inputParams.put("input", files);
        InputNode inputNode = new InputNode(10L, "Input", inputParams);
        nodes.add(inputNode);

        // Create GaussianBlur node (id: 1)
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", new NodeReference("@node:10:foo"));
        gaussianParams.put("sizeX", 33);
        gaussianParams.put("sizeY", 33);
        gaussianParams.put("sigmaX", 1.5);
        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
        GaussianBlurNode gaussianNode = new GaussianBlurNode(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);

        InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () -> new Graph(nodes));
        assertTrue(exception.getMessage().contains("Invalid node reference: Node with id 10 does not contain output 'foo'"));
    }

    @Nested
    @DisplayName("Graph Overflow Tests")
    class GraphOverflowTests {
        @Test
        void integerOverflow_shouldThrowInvalidNodeParameter() {
            List<Node> nodes = new ArrayList<>();

            Map<String, Object> gaussianParams = new HashMap<>();
            gaussianParams.put("files", new ArrayList<>());
            gaussianParams.put("sizeX", (long) Integer.MAX_VALUE + 1);
            gaussianParams.put("sizeY", 5);
            gaussianParams.put("sigmaX", 1.5);
            gaussianParams.put("sigmaY", 1.5);
            GaussianBlurNode gaussianNode = new GaussianBlurNode(1L, "GaussianBlur", gaussianParams);
            nodes.add(gaussianNode);

            InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () -> new Graph(nodes));
            assertTrue(exception.getMessage().contains("exceeds integer range"));
        }

        @Test
        void integerUnderflow_shouldThrowInvalidNodeParameter() {
            List<Node> nodes = new ArrayList<>();

            Map<String, Object> gaussianParams = new HashMap<>();
            gaussianParams.put("files", new ArrayList<>());
            gaussianParams.put("sizeX", (long) Integer.MIN_VALUE - 1);
            gaussianParams.put("sizeY", 5);
            gaussianParams.put("sigmaX", 1.5);
            gaussianParams.put("sigmaY", 1.5);
            GaussianBlurNode gaussianNode = new GaussianBlurNode(1L, "GaussianBlur", gaussianParams);
            nodes.add(gaussianNode);

            InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () -> new Graph(nodes));
            assertTrue(exception.getMessage().contains("exceeds integer range"));
        }

        @Test
        void doublePositiveInfinity_shouldThrowInvalidNodeParameter() {
            List<Node> nodes = new ArrayList<>();

            // Create a BilateralFilter node with infinite sigma color
            Map<String, Object> bilateralParams = new HashMap<>();
            bilateralParams.put("files", new ArrayList<>());
            bilateralParams.put("d", 9);
            bilateralParams.put("sigmaColor", Double.POSITIVE_INFINITY);  // Invalid double value
            bilateralParams.put("sigmaSpace", 75.0);
            BilateralFilterNode bilateralNode = new BilateralFilterNode(1L, "BilateralFilter", bilateralParams);
            nodes.add(bilateralNode);

            InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () -> new Graph(nodes));
            assertTrue(exception.getMessage().contains("too large or not a valid number"));
        }

        @Test
        void doubleNegativeInfinity_shouldThrowInvalidNodeParameter() {
            List<Node> nodes = new ArrayList<>();

            // Create a BilateralFilter node with negative infinite sigma space
            Map<String, Object> bilateralParams = new HashMap<>();
            bilateralParams.put("files", new ArrayList<>());
            bilateralParams.put("d", 9);
            bilateralParams.put("sigmaColor", 75.0);
            bilateralParams.put("sigmaSpace", Double.NEGATIVE_INFINITY);  // Invalid double value
            BilateralFilterNode bilateralNode = new BilateralFilterNode(1L, "BilateralFilter", bilateralParams);
            nodes.add(bilateralNode);

            InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () -> new Graph(nodes));
            assertTrue(exception.getMessage().contains("too large or not a valid number"));
        }

        @Test
        void doubleNaN_shouldThrowInvalidNodeParameter() {
            List<Node> nodes = new ArrayList<>();

            // Create a BilateralFilter node with NaN value
            Map<String, Object> bilateralParams = new HashMap<>();
            bilateralParams.put("files", new ArrayList<>());
            bilateralParams.put("d", 9);
            bilateralParams.put("sigmaColor", Double.NaN);  // Not a number
            bilateralParams.put("sigmaSpace", 75.0);
            BilateralFilterNode bilateralNode = new BilateralFilterNode(1L, "BilateralFilter", bilateralParams);
            nodes.add(bilateralNode);

            InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () -> new Graph(nodes));
            assertTrue(exception.getMessage().contains("not a valid number"));
        }
    }
}
