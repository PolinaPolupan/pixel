package com.example.mypixel.service;

import com.example.mypixel.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NodeProcessorService {

    private final AutowireCapableBeanFactory beanFactory;
    // Hash map to store node IDs and their corresponding outputs
    private final Map<Long, Map<String, Object>> nodeOutputs = new HashMap<>();
    private final StorageService tempStorageService;
    private final StorageService storageService;

    @Autowired
    public NodeProcessorService(
            AutowireCapableBeanFactory beanFactory,
            @Qualifier("storageService") StorageService storageService,
            @Qualifier("tempStorageService") StorageService tempStorageService
    ) {
        this.beanFactory = beanFactory;
        this.tempStorageService = tempStorageService;
        this.storageService = storageService;
    }

    public void processNode(Node node) {
        beanFactory.autowireBean(node);
        nodeOutputs.computeIfAbsent(node.getId(), k -> new HashMap<>());
        nodeOutputs.put(node.getId(), node.exec(resolveInputs(node)));
        log.info(nodeOutputs.toString());
    }

    private Object resolveReference(NodeReference reference) {
        if (reference.getOutputName().equals("files")) {
            List<String> files = new ArrayList<>();
            for (String value : (List<String>) nodeOutputs.get(reference.getNodeId()).get("files")) {
                String temp = tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource(value));
                files.add(temp);
            }
            return files;
        }
        return nodeOutputs.get(reference.getNodeId()).get(reference.getOutputName());
    }

    private Map<String, Object> resolveInputs(Node node) {
        Map<String, Object> processedInputs = new HashMap<>();

        for (String key: node.getInputTypes().keySet()) {
            if (node.getInputs().containsKey(key)) {
                Object input = node.getInputs().get(key);
                if (input instanceof NodeReference) {
                    processedInputs.put(key, resolveReference((NodeReference) input));
                } else {
                    processedInputs.put(key, input);
                }
            }
        }
        return processedInputs;
    }

    public void clear() {
        nodeOutputs.clear();
        tempStorageService.deleteAll();
        tempStorageService.init();
    }
}
