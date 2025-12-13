package com.example.pixel.graph.model;

import com.example.pixel.graph.dto.GraphDto;
import com.example.pixel.graph.service.GraphBuilder;
import com.example.pixel.node_execution.model.Node;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Graph {
    private final List<Node> topologicalOrder;
    private final List<List<Node>> levels;

    public Graph(GraphDto graphDto) {
        List<Node> nodes = GraphBuilder.setupReferences(graphDto.getNodes());
        Map<Long, Node> nodeMap = GraphBuilder.getNodeMap(nodes);
        Map<Node, List<Node>> nodeOutputs = GraphBuilder.mapOutputNodes(nodes);

        this.levels = GraphBuilder.buildTopologicalOrder(nodes, nodeOutputs, nodeMap);
        this.topologicalOrder = GraphBuilder.getTopologicalOrderFromLevels(levels);
    }

    public Iterator<List<Node>> levelIterator() {
        return new LevelIterator(levels);
    }
    public Iterator<Node> nodeIterator() {
        return new NodeIterator(topologicalOrder);
    }
}