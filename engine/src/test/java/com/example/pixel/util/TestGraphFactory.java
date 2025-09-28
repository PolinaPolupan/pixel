//package com.example.pixel.util;
//
//import com.example.pixel.graph.dto.GraphPayload;
//import com.example.pixel.node_execution.model.NodeExecution;
//import com.example.pixel.node_execution.model.NodeReference;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class TestGraphFactory {
//
//    public static GraphPayload getDefaultGraph(Long sceneId) {
//        Map<String, Object> inputParams = new HashMap<>();
//        List<String> files = new ArrayList<>();
//        files.add("scenes/" + sceneId + "/input/Picture1.png");
//        files.add("scenes/" + sceneId + "/input/Picture3.png");
//        inputParams.put("input", files);
//
//        Map<String, Object> outputParams = new HashMap<>();
//        outputParams.put("files", new NodeReference("@node:6:files"));
//        outputParams.put("prefix", new NodeReference("@node:7:output"));
//        outputParams.put("folder", "processed");
//
//        return getDefaultGraph(inputParams, outputParams);
//    }
//
//    public static GraphPayload getDefaultGraph(
//            Map<String, Object> inputParams,
//            Map<String, Object> outputParams
//    ) {
//        List<NodeExecution> nodeExecutions = new ArrayList<>();
//
//        // Create Input node (id: 1)
//        NodeExecution inputNodeExecution = new NodeExecution(1L, "Input", inputParams);
//        nodeExecutions.add(inputNodeExecution);
//
//        // Create Vector2D node (id: 2)
//        Map<String, Object> vectorParams = new HashMap<>();
//        vectorParams.put("x", 5);
//        vectorParams.put("y", 5);
//        NodeExecution vector2DNodeExecution = new NodeExecution(2L, "Vector2D", vectorParams);
//        nodeExecutions.add(vector2DNodeExecution);
//
//        // Create Floor node (id: 3)
//        Map<String, Object> floorParams = new HashMap<>();
//        floorParams.put("input", 2.5);
//        NodeExecution floorNodeExecution = new NodeExecution(3L, "Floor", floorParams);
//        nodeExecutions.add(floorNodeExecution);
//
//        // Create Blur node (id: 4)
//        Map<String, Object> blurParams = new HashMap<>();
//        blurParams.put("files", new NodeReference("@node:1:output"));
//        blurParams.put("ksize", new NodeReference("@node:2:vector2D"));
//        NodeExecution blurNodeExecution = new NodeExecution(4L, "Blur", blurParams);
//        nodeExecutions.add(blurNodeExecution);
//
//        // Create GaussianBlur node (id: 5)
//        Map<String, Object> gaussianParams = new HashMap<>();
//        gaussianParams.put("files", new NodeReference("@node:4:files"));
//        gaussianParams.put("sizeX", 5);
//        gaussianParams.put("sizeY", 5);
//        gaussianParams.put("sigmaX", 1.5);
//        gaussianParams.put("sigmaY", 1.5);
//        NodeExecution gaussianNodeExecution = new NodeExecution(5L, "GaussianBlur", gaussianParams);
//        nodeExecutions.add(gaussianNodeExecution);
//
//        // Create BilateralFilter node (id: 6)
//        Map<String, Object> bilateralParams = new HashMap<>();
//        bilateralParams.put("files", new NodeReference("@node:5:files"));
//        bilateralParams.put("d", 9);
//        bilateralParams.put("sigmaColor", 75.0);
//        bilateralParams.put("sigmaSpace", 75.0);
//        NodeExecution bilateralNodeExecution = new NodeExecution(6L, "BilateralFilter", bilateralParams);
//        nodeExecutions.add(bilateralNodeExecution);
//
//        // Create String node (id: 7)
//        Map<String, Object> stringParams = new HashMap<>();
//        stringParams.put("input", "filtered_result");
//        NodeExecution stringNodeExecution = new NodeExecution(7L, "String", stringParams);
//        nodeExecutions.add(stringNodeExecution);
//
//        // Create Output node (id: 8)
//        NodeExecution outputNodeExecution = new NodeExecution(8L, "Output", outputParams);
//        nodeExecutions.add(outputNodeExecution);
//
//        return new GraphPayload(nodeExecutions);
//    }
//
//    public static GraphPayload getMinimalGraph() {
//        List<NodeExecution> nodeExecutions = new ArrayList<>();
//
//        // Create Input node (id: 10)
//        Map<String, Object> inputParams = new HashMap<>();
//        List<String> files = new ArrayList<>();
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
//        // Create Output node (id: 2)
//        Map<String, Object> outputParams = new HashMap<>();
//        outputParams.put("files", new NodeReference("@node:10:output"));
//        outputParams.put("prefix", "output1");
//        outputParams.put("folder", "output_1");
//        NodeExecution outputNodeExecution = new NodeExecution(2L, "Output", outputParams);
//        nodeExecutions.add(outputNodeExecution);
//
//        return new GraphPayload(nodeExecutions);
//    }
//}
