//package com.example.pixel.graph;
//
//import com.example.pixel.common.exception.InvalidGraphException;
//import com.example.pixel.common.exception.InvalidNodeInputException;
//import com.example.pixel.graph.model.Graph;
//import com.example.pixel.node_execution.model.NodeExecution;
//import com.example.pixel.node_execution.model.NodeReference;
//import com.example.pixel.util.TestGraphFactory;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//
//public class GraphTest {
//
//    private final Long sceneId = 1L;
//
//    @Test
//    void  constructValidGraph_shouldSucceed() {
//        Graph graph = TestGraphFactory.getDefaultGraph(sceneId).toGraph();
//
//        assertNotNull(graph);
//        assertEquals(8, graph.getNodeExecutions().size());
//        assertEquals(8, graph.getTopologicalOrder().size());
//    }
//
//    @Test
//    void constructGraphWithDuplicateIds_shouldThrowInvalidGraphException() {
//        List<NodeExecution> nodeExecutions = new ArrayList<>();
//
//        Map<String, Object> floorParams = new HashMap<>();
//        floorParams.put("input", 56);
//        NodeExecution floorNodeExecution1 = new NodeExecution(4L, "Floor", floorParams);
//        nodeExecutions.add(floorNodeExecution1);
//
//        NodeExecution floorNodeExecution2 = new NodeExecution(4L, "Floor", floorParams);
//        nodeExecutions.add(floorNodeExecution2);
//
//        InvalidGraphException exception = assertThrows(InvalidGraphException.class, () -> new Graph(1L, nodeExecutions));
//        assertTrue(exception.getMessage().contains("duplicate IDs"));
//        assertTrue(exception.getMessage().contains("4"));
//    }
//
//    @Test
//    void nodeOutputs_shouldBeCorrectlyIdentified() {
//        // Create a graph with the template nodes
//        Graph graph = TestGraphFactory.getDefaultGraph(sceneId).toGraph();
//        List<NodeExecution> nodeExecutions = graph.getNodeExecutions();
//
//        // Get the node outputs map
//        Map<NodeExecution, List<NodeExecution>> nodeOutputs = graph.getNodeOutputs();
//
//        // Reference nodes by their index in the list for clarity
//        NodeExecution inputNodeExecution = nodeExecutions.get(0);      // Input node (id: 1)
//        NodeExecution vectorNodeExecution = nodeExecutions.get(1);     // Vector2D node (id: 2)
//        NodeExecution floorNodeExecution = nodeExecutions.get(2);      // Floor node (id: 3)
//        NodeExecution blurNodeExecution = nodeExecutions.get(3);       // Blur node (id: 4)
//        NodeExecution gaussianNodeExecution = nodeExecutions.get(4);   // GaussianBlur node (id: 5)
//        NodeExecution bilateralNodeExecution = nodeExecutions.get(5);  // BilateralFilter node (id: 6)
//        NodeExecution stringNodeExecution = nodeExecutions.get(6);     // String node (id: 7)
//        NodeExecution outputNodeExecution = nodeExecutions.get(7);     // Output node (id: 8)
//
//        // Verify Input node (1) outputs: should connect to Blur node (4)
//        assertEquals(1, nodeOutputs.get(inputNodeExecution).size(), "Input node should have 1 output");
//        assertTrue(nodeOutputs.get(inputNodeExecution).contains(blurNodeExecution), "Input node should connect to Blur node");
//
//        // Verify Vector2D node (2) outputs: should connect to Blur node (4)
//        assertEquals(1, nodeOutputs.get(vectorNodeExecution).size(), "Vector2D node should have 1 output");
//        assertTrue(nodeOutputs.get(vectorNodeExecution).contains(blurNodeExecution), "Vector2D node should connect to Blur node");
//
//        // Verify Floor node (3) outputs: should have no connections
//        assertTrue(nodeOutputs.get(floorNodeExecution).isEmpty(), "Floor node should have no outputs");
//
//        // Verify Blur node (4) outputs: should connect to GaussianBlur node (5)
//        assertEquals(1, nodeOutputs.get(blurNodeExecution).size(), "Blur node should have 1 output");
//        assertTrue(nodeOutputs.get(blurNodeExecution).contains(gaussianNodeExecution), "Blur node should connect to GaussianBlur node");
//
//        // Verify GaussianBlur node (5) outputs: should connect to BilateralFilter node (6)
//        assertEquals(1, nodeOutputs.get(gaussianNodeExecution).size(), "GaussianBlur node should have 1 output");
//        assertTrue(nodeOutputs.get(gaussianNodeExecution).contains(bilateralNodeExecution), "GaussianBlur node should connect to BilateralFilter node");
//
//        // Verify BilateralFilter node (6) outputs: should connect to Output node (8)
//        assertEquals(1, nodeOutputs.get(bilateralNodeExecution).size(), "BilateralFilter node should have 1 output");
//        assertTrue(nodeOutputs.get(bilateralNodeExecution).contains(outputNodeExecution), "BilateralFilter node should connect to Output node");
//
//        // Verify String node (7) outputs: should connect to Output node (8)
//        assertEquals(1, nodeOutputs.get(stringNodeExecution).size(), "String node should have 1 output");
//        assertTrue(nodeOutputs.get(stringNodeExecution).contains(outputNodeExecution), "String node should connect to Output node");
//
//        // Verify Output node (8) outputs: should have no connections
//        assertTrue(nodeOutputs.get(outputNodeExecution).isEmpty(), "Output node should have no outputs");
//    }
//
//    @Test
//    void topologicalSort_shouldProduceCorrectOrdering() {
//        Graph graph = TestGraphFactory.getDefaultGraph(sceneId).toGraph();
//        List<NodeExecution> nodeExecutions = graph.getNodeExecutions();
//
//        // Get the topological ordering
//        List<NodeExecution> topologicalOrder = graph.getTopologicalOrder();
//
//        // Verify the size matches the number of nodes
//        assertEquals(8, topologicalOrder.size(), "Topological order should contain all nodes");
//
//        // Get references to all nodes by their index for readability
//        NodeExecution inputNodeExecution = nodeExecutions.get(0);      // Input node (id: 1)
//        NodeExecution vectorNodeExecution = nodeExecutions.get(1);     // Vector2D node (id: 2)
//        NodeExecution floorNodeExecution = nodeExecutions.get(2);      // Floor node (id: 3)
//        NodeExecution blurNodeExecution = nodeExecutions.get(3);       // Blur node (id: 4)
//        NodeExecution gaussianNodeExecution = nodeExecutions.get(4);   // GaussianBlur node (id: 5)
//        NodeExecution bilateralNodeExecution = nodeExecutions.get(5);  // BilateralFilter node (id: 6)
//        NodeExecution stringNodeExecution = nodeExecutions.get(6);     // String node (id: 7)
//        NodeExecution outputNodeExecution = nodeExecutions.get(7);     // Output node (id: 8)
//
//        // Find positions of each node in the topological order
//        int posInput = topologicalOrder.indexOf(inputNodeExecution);
//        int posVector = topologicalOrder.indexOf(vectorNodeExecution);
//        int posFloor = topologicalOrder.indexOf(floorNodeExecution);
//        int posBlur = topologicalOrder.indexOf(blurNodeExecution);
//        int posGaussian = topologicalOrder.indexOf(gaussianNodeExecution);
//        int posBilateral = topologicalOrder.indexOf(bilateralNodeExecution);
//        int posString = topologicalOrder.indexOf(stringNodeExecution);
//        int posOutput = topologicalOrder.indexOf(outputNodeExecution);
//
//        // Verify all nodes are present in the order
//        assertNotEquals(-1, posInput, "Input node missing from topological order");
//        assertNotEquals(-1, posVector, "Vector2D node missing from topological order");
//        assertNotEquals(-1, posFloor, "Floor node missing from topological order");
//        assertNotEquals(-1, posBlur, "Blur node missing from topological order");
//        assertNotEquals(-1, posGaussian, "GaussianBlur node missing from topological order");
//        assertNotEquals(-1, posBilateral, "BilateralFilter node missing from topological order");
//        assertNotEquals(-1, posString, "String node missing from topological order");
//        assertNotEquals(-1, posOutput, "Output node missing from topological order");
//
//        // Verify dependency relationships:
//
//        // Input node must come before Blur node
//        assertTrue(posInput < posBlur,
//                "Input node (1) must come before Blur node (4) in topological order");
//
//        // Vector2D node must come before Blur node
//        assertTrue(posVector < posBlur,
//                "Vector2D node (2) must come before Blur node (4) in topological order");
//
//        // Blur node must come before GaussianBlur node
//        assertTrue(posBlur < posGaussian,
//                "Blur node (4) must come before GaussianBlur node (5) in topological order");
//
//        // GaussianBlur node must come before BilateralFilter node
//        assertTrue(posGaussian < posBilateral,
//                "GaussianBlur node (5) must come before BilateralFilter node (6) in topological order");
//
//        // BilateralFilter node must come before Output node
//        assertTrue(posBilateral < posOutput,
//                "BilateralFilter node (6) must come before Output node (8) in topological order");
//
//        // String node must come before Output node
//        assertTrue(posString < posOutput,
//                "String node (7) must come before Output node (8) in topological order");
//
//        // The FloorNode has no connections, so it can be anywhere in the ordering
//        // But we can still verify it's in the ordering
//        assertTrue(posFloor >= 0, "Floor node (3) must be in the topological order");
//    }
//
//    @Test
//    void graphWithCycle_shouldThrowInvalidGraphException() {
//        List<NodeExecution> nodeExecutions = new ArrayList<>();
//
//        // Create Input node (id: 10)
//        Map<String, Object> inputParams = new HashMap<>();
//        List<String> files = new ArrayList<>();
//        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture1.png");
//        files.add("upload-image-dir/scenes/" + sceneId + "/input/Picture3.png");
//        inputParams.put("input", files);
//        NodeExecution inputNodeExecution = new NodeExecution(10L, "Input", inputParams);
//        nodeExecutions.add(inputNodeExecution);
//
//        // Create Floor node (id: 4)
//        Map<String, Object> floorParams = new HashMap<>();
//        floorParams.put("input", 56);
//        NodeExecution floorNodeExecution = new NodeExecution(4L, "Floor", floorParams);
//        nodeExecutions.add(floorNodeExecution);
//
//        // Create GaussianBlur node (id: 1)
//        Map<String, Object> gaussianParams = new HashMap<>();
//        gaussianParams.put("files", new NodeReference("@node:10:output"));
//        gaussianParams.put("sizeX", 33);
//        gaussianParams.put("sizeY", 33);
//        gaussianParams.put("sigmaX", new NodeReference("@node:4:output"));
//        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
//        NodeExecution gaussianNodeExecution = new NodeExecution(1L, "GaussianBlur", gaussianParams);
//        nodeExecutions.add(gaussianNodeExecution);
//
//        // Create GaussianBlur node (id: 2)
//        Map<String, Object> gaussianParams2 = new HashMap<>();
//        gaussianParams2.put("files", new NodeReference("@node:3:files"));
//        gaussianParams2.put("sizeX", 33);
//        gaussianParams2.put("sizeY", 33);
//        gaussianParams2.put("sigmaX", new NodeReference("@node:4:output"));
//        gaussianParams2.put("sigmaY", 1.5); // Not in JSON, using default value
//        NodeExecution gaussianNodeExecution2 = new NodeExecution(2L, "GaussianBlur", gaussianParams2);
//        nodeExecutions.add(gaussianNodeExecution2);
//
//        // Create GaussianBlur node (id: 3)
//        Map<String, Object> gaussianParams3 = new HashMap<>();
//        gaussianParams3.put("files", new NodeReference("@node:2:files"));
//        gaussianParams3.put("sizeX", 33);
//        gaussianParams3.put("sizeY", 33);
//        gaussianParams3.put("sigmaX", new NodeReference("@node:4:output"));
//        gaussianParams3.put("sigmaY", 1.5); // Not in JSON, using default value
//        NodeExecution gaussianNodeExecution3 = new NodeExecution(3L, "GaussianBlur", gaussianParams3);
//        nodeExecutions.add(gaussianNodeExecution3);
//
//        InvalidGraphException exception = assertThrows(InvalidGraphException.class, () -> new Graph(1L, nodeExecutions));
//        assertTrue(exception.getMessage().contains("cycle"));
//    }
//
//    @Test
//    void graphWithSelfReference_shouldThrowInvalidGraphException() {
//        List<NodeExecution> nodeExecutions = new ArrayList<>();
//
//        // Create GaussianBlur node (id: 1)
//        Map<String, Object> gaussianParams = new HashMap<>();
//        gaussianParams.put("files", new NodeReference("@node:1:files"));
//        gaussianParams.put("sizeX", 33);
//        gaussianParams.put("sizeY", 33);
//        gaussianParams.put("sigmaX", 5);
//        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
//        NodeExecution gaussianNodeExecution = new NodeExecution(1L, "GaussianBlur", gaussianParams);
//        nodeExecutions.add(gaussianNodeExecution);
//
//        InvalidGraphException exception = assertThrows(InvalidGraphException.class, () -> new Graph(1L, nodeExecutions));
//        assertTrue(exception.getMessage().contains("cycle"));
//    }
//
//    @Test
//    void graphWithInvalidReferenceId_shouldThrowInvalidGraphException() {
//        List<NodeExecution> nodeExecutions = new ArrayList<>();
//
//        // Create GaussianBlur node (id: 1)
//        Map<String, Object> gaussianParams = new HashMap<>();
//        gaussianParams.put("files", new NodeReference("@node:10:files"));
//        gaussianParams.put("sizeX", 33);
//        gaussianParams.put("sizeY", 33);
//        gaussianParams.put("sigmaX", 5);
//        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
//        NodeExecution gaussianNodeExecution = new NodeExecution(1L, "GaussianBlur", gaussianParams);
//        nodeExecutions.add(gaussianNodeExecution);
//
//        InvalidNodeInputException exception = assertThrows(InvalidNodeInputException.class, () -> new Graph(1L, nodeExecutions));
//        assertTrue(exception.getMessage().contains("Invalid node reference: Node with id 10 is not found"));
//    }
//}
