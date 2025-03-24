package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.Node;
import com.example.mypixel.model.NodeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NodeProcessorService {

    // Hash map to store node IDs and their corresponding outputs
    private final Map<Long, List<Map<String, Object>>> nodeOutputs = new HashMap<>();
    private final StorageService tempStorageService;
    private final StorageService storageService;
    private final FilteringService filteringService;

    @Autowired
    public NodeProcessorService(
            @Qualifier("storageService") StorageService storageService,
            @Qualifier("tempStorageService") StorageService tempStorageService,
            FilteringService filteringService) {
        this.tempStorageService = tempStorageService;
        this.storageService = storageService;
        this.filteringService = filteringService;
    }

    public void processInputNode(Node node) {
        nodeOutputs.computeIfAbsent(node.getId(), k -> new ArrayList<>());
        List<Map<String, Object>> outputs = new ArrayList<>();
        List<String> inputs = (List<String>) node.getInputs().get("files");

        for (String input: inputs) {
            String temp = tempStorageService.createTempFileFromResource(storageService.loadAsResource(input));
            outputs.add(Map.of("file", temp));
        }
        nodeOutputs.get(node.getId()).addAll(outputs);
        log.info(nodeOutputs.toString());
    }

    public void processGaussianBlurNode(Node node) {
        nodeOutputs.computeIfAbsent(node.getId(), k -> new ArrayList<>());
        List<Map<String, Object>> outputs = new ArrayList<>();
        List<Map<String, Object>> inputs = new ArrayList<>();

        if (node.getInputs().get("file") instanceof NodeReference) {
            for (Map<String, Object> values: nodeOutputs.get(((NodeReference) node.getInputs().get("file")).getNodeId())) {
                int sizeX = (int) node.getInputs().getOrDefault("sizeX", 1);
                int sizeY = (int) node.getInputs().getOrDefault("sizeY", 1);
                double sigmaX = (double) node.getInputs().getOrDefault("sigmaX", 0.0);
                double sigmaY = (double) node.getInputs().getOrDefault("sigmaY", 0.0);

                inputs.add(Map.of(
                        "file", values.get("file"),
                        "sizeX", sizeX,
                        "sizeY", sizeY,
                        "sigmaX", sigmaX,
                        "sigmaY", sigmaY
                ));
            }
        }

        for (Map<String, Object> input: inputs) {
            String temp = tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource((String) input.get("file")));
            filteringService.gaussianBlur(
                    temp,
                    (int) input.get("sizeX"),
                    (int) input.get("sizeY"),
                    (double) input.get("sigmaX"),
                    (double) input.get("sigmaY"));
            outputs.add(Map.of("file", temp));
        }

        nodeOutputs.get(node.getId()).addAll(outputs);
        log.info(inputs.toString());
        log.info(nodeOutputs.toString());
    }

    public void processOutputNode(Node node) {
        nodeOutputs.computeIfAbsent(node.getId(), k -> new ArrayList<>());
        List<Map<String, Object>> outputs = new ArrayList<>();
        List<Map<String, Object>> inputs = new ArrayList<>();

        if (node.getInputs().get("file") instanceof NodeReference) {
            for (Map<String, Object> values: nodeOutputs.get(((NodeReference) node.getInputs().get("file")).getNodeId())) {
                String prefix = (String) node.getInputs().getOrDefault("prefix", "");

                inputs.add(Map.of("file", values.get("file"), "prefix", prefix));
            }
        }

        for (Map<String, Object> input: inputs) {
            String temp = tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource((String) input.get("file")));
            String filename = tempStorageService.removeExistingPrefix(temp);
            if (node.getInputs().get("prefix") != null) {
                filename = node.getInputs().get("prefix") + "_" + filename;
            }

            storageService.store(tempStorageService.loadAsResource(temp), filename);
            outputs.add(Map.of("file", temp));
        }

        log.info(inputs.toString());
        nodeOutputs.get(node.getId()).addAll(outputs);
        log.info(nodeOutputs.toString());
    }
}
