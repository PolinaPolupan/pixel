package com.example.mypixel.service;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.model.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;


@Service
@Slf4j
public class GraphService {

    private final NodeProcessorService nodeProcessorService;

    @Autowired
    public GraphService(NodeProcessorService nodeProcessorService) {
        this.nodeProcessorService = nodeProcessorService;
    }

    public void processGraph(Graph graph) {
        // Initialize temporary storage for processing artifacts
        nodeProcessorService.clear();

        Iterator<Node> iterator = graph.iterator();

        while (iterator.hasNext()) {
            Node node = iterator.next();

            NodeType type = node.getType();

            if (type.equals(NodeType.INPUT)) {
                nodeProcessorService.processInputNode(node);
            }
            if (type.equals(NodeType.GAUSSIAN_BLUR)) {
                nodeProcessorService.processGaussianBlurNode(node);
            }
            if (type.equals(NodeType.OUTPUT)) {
                nodeProcessorService.processOutputNode(node);
            }

            log.info("Node with id: {} is processed", node.getId());
        }
    }
}
