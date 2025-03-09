package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
public class GraphService {

    private final StorageService storageService;

    @Autowired
    public GraphService(StorageService storageService) {
        this.storageService = storageService;
    }

    private final HashSet<String> nodesTypes = new HashSet<>(Arrays.asList("InputNode", "OutputNode"));

    public void  processGraph(Graph graph) {
        Resource outputImage = null;
        for (Node node : graph.getNodes()) {
            if (!nodesTypes.contains(node.getType())) {
                throw new InvalidNodeType("Invalid node type: " + node.getType());
            }
            if (node.getType().equals("InputNode")) {
                outputImage = storageService.loadAsResource((String) node.getParams().get("filename"));
                log.info("InputNode processed");
            }
            if (node.getType().equals("OutputNode")) {
                storageService.store(outputImage, (String) node.getParams().get("filename"));
                log.info("OutputNode processed");
            }
        }
    }
}
