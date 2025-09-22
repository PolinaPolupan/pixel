package com.example.pixel.util;

import com.example.pixel.execution.model.ExecutionGraphPayload;
import com.example.pixel.node.model.Node;
import com.example.pixel.node.model.NodeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestGraphFactory {

    public static ExecutionGraphPayload getDefaultGraph(Long sceneId) {
        Map<String, Object> inputParams = new HashMap<>();
        List<String> files = new ArrayList<>();
        files.add("scenes/" + sceneId + "/input/Picture1.png");
        files.add("scenes/" + sceneId + "/input/Picture3.png");
        inputParams.put("input", files);

        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("files", new NodeReference("@node:6:files"));
        outputParams.put("prefix", new NodeReference("@node:7:output"));
        outputParams.put("folder", "processed");

        return getDefaultGraph(inputParams, outputParams);
    }

    public static ExecutionGraphPayload getDefaultGraph(
            Map<String, Object> inputParams,
            Map<String, Object> outputParams
    ) {
        List<Node> nodes = new ArrayList<>();

        // Create Input node (id: 1)
        Node inputNode = new Node(1L, "Input", inputParams);
        nodes.add(inputNode);

        // Create Vector2D node (id: 2)
        Map<String, Object> vectorParams = new HashMap<>();
        vectorParams.put("x", 5);
        vectorParams.put("y", 5);
        Node vector2DNode = new Node(2L, "Vector2D", vectorParams);
        nodes.add(vector2DNode);

        // Create Floor node (id: 3)
        Map<String, Object> floorParams = new HashMap<>();
        floorParams.put("input", 2.5);
        Node floorNode = new Node(3L, "Floor", floorParams);
        nodes.add(floorNode);

        // Create Blur node (id: 4)
        Map<String, Object> blurParams = new HashMap<>();
        blurParams.put("files", new NodeReference("@node:1:output"));
        blurParams.put("ksize", new NodeReference("@node:2:vector2D"));
        Node blurNode = new Node(4L, "Blur", blurParams);
        nodes.add(blurNode);

        // Create GaussianBlur node (id: 5)
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", new NodeReference("@node:4:files"));
        gaussianParams.put("sizeX", 5);
        gaussianParams.put("sizeY", 5);
        gaussianParams.put("sigmaX", 1.5);
        gaussianParams.put("sigmaY", 1.5);
        Node gaussianNode = new Node(5L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);

        // Create BilateralFilter node (id: 6)
        Map<String, Object> bilateralParams = new HashMap<>();
        bilateralParams.put("files", new NodeReference("@node:5:files"));
        bilateralParams.put("d", 9);
        bilateralParams.put("sigmaColor", 75.0);
        bilateralParams.put("sigmaSpace", 75.0);
        Node bilateralNode = new Node(6L, "BilateralFilter", bilateralParams);
        nodes.add(bilateralNode);

        // Create String node (id: 7)
        Map<String, Object> stringParams = new HashMap<>();
        stringParams.put("input", "filtered_result");
        Node stringNode = new Node(7L, "String", stringParams);
        nodes.add(stringNode);

        // Create Output node (id: 8)
        Node outputNode = new Node(8L, "Output", outputParams);
        nodes.add(outputNode);

        return new ExecutionGraphPayload(nodes);
    }

    public static ExecutionGraphPayload getMinimalGraph() {
        List<Node> nodes = new ArrayList<>();

        // Create Input node (id: 10)
        Map<String, Object> inputParams = new HashMap<>();
        List<String> files = new ArrayList<>();
        inputParams.put("input", files);
        Node inputNode = new Node(10L, "Input", inputParams);
        nodes.add(inputNode);

        // Create Floor node (id: 4)
        Map<String, Object> floorParams = new HashMap<>();
        floorParams.put("input", 56);
        Node floorNode = new Node(4L, "Floor", floorParams);
        nodes.add(floorNode);

        // Create GaussianBlur node (id: 1)
        Map<String, Object> gaussianParams = new HashMap<>();
        gaussianParams.put("files", new NodeReference("@node:10:output"));
        gaussianParams.put("sizeX", 33);
        gaussianParams.put("sizeY", 33);
        gaussianParams.put("sigmaX", new NodeReference("@node:4:output"));
        gaussianParams.put("sigmaY", 1.5); // Not in JSON, using default value
        Node gaussianNode = new Node(1L, "GaussianBlur", gaussianParams);
        nodes.add(gaussianNode);

        // Create Output node (id: 2)
        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("files", new NodeReference("@node:10:output"));
        outputParams.put("prefix", "output1");
        outputParams.put("folder", "output_1");
        Node outputNode = new Node(2L, "Output", outputParams);
        nodes.add(outputNode);

        return new ExecutionGraphPayload(nodes);
    }
}
