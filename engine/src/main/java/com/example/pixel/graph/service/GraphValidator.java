package com.example.pixel.graph.service;

import com.example.pixel.common.exception.InvalidGraphException;
import com.example.pixel.common.exception.InvalidNodeInputException;
import com.example.pixel.graph.dto.CreateGraphRequest;
import com.example.pixel.node.dto.NodeConfigurationDto;
import com.example.pixel.node.service.NodeService;
import com.example.pixel.node_execution.model.Node;
import com.example.pixel.node_execution.model.NodeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class GraphValidator {

    private final NodeService nodeService;

    /**
     * Validates node references - ensures all referenced nodes exist
     */
    public void validateReferences(List<Node> nodes, Map<Long, Node> nodeMap) {
        Map<String, NodeConfigurationDto> nodeTypes = nodeService.getAllActiveNodes();

        for (Node node : nodes) {
            // Validate node type exists
            if (!nodeTypes.containsKey(node.getType())) {
                throw new InvalidNodeInputException("Invalid node type: " + node.getType() + " is not a registered node type.");
            }

            NodeConfigurationDto nodeConfig = nodeTypes.get(node.getType());

            for (String paramName : node.getInputs().keySet()) {
                Object paramValue = node.getInputs().get(paramName);

                // Validate parameter exists in node configuration
                if (!nodeConfig.getInputs().containsKey(paramName)) {
                    throw new InvalidNodeInputException("Invalid parameter '" + paramName + "' for node type: " + node.getType());
                }

                if (paramValue instanceof NodeReference) {
                    Long targetNodeId = ((NodeReference) paramValue).getNodeId();

                    if (! nodeMap.containsKey(targetNodeId)) {
                        throw new InvalidNodeInputException(
                                "Invalid node reference: Node with id " + targetNodeId +
                                        " is not found. Please ensure the node id is correct."
                        );
                    }
                }
            }
        }
    }

    /**
     * Validates that there are no duplicate node IDs
     */
    public void validateNoDuplicateIds(List<Node> nodes) {
        Set<Long> seenIds = new HashSet<>();
        List<Long> duplicateIds = new ArrayList<>();

        for (Node node : nodes) {
            if (!seenIds.add(node.getId())) {
                duplicateIds.add(node. getId());
            }
        }

        if (!duplicateIds.isEmpty()) {
            throw new InvalidGraphException("Graph contains nodes with duplicate IDs: " + duplicateIds);
        }
    }

    /**
     * Detects cycles in the graph using DFS
     */
    public void validateNoCycles(List<Node> nodes, Map<Node, List<Node>> nodeOutputs) {
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();
        Map<Long, Long> parent = new HashMap<>();

        for (Node node : nodes) {
            if (!visited.contains(node.getId())) {
                Long cycleNode = detectCycleDFS(
                        node,
                        nodeOutputs,
                        visited,
                        recursionStack,
                        parent
                );

                if (cycleNode != null) {
                    List<Long> cyclePath = buildCyclePath(cycleNode, parent);
                    throw new InvalidGraphException("Graph contains a cycle: " + cyclePath);
                }
            }
        }
    }

    /**
     * DFS helper method to detect cycles
     */
    private Long detectCycleDFS(
            Node current,
            Map<Node, List<Node>> nodeOutputs,
            Set<Long> visited,
            Set<Long> recursionStack,
            Map<Long, Long> parent) {

        visited.add(current.getId());
        recursionStack.add(current.getId());

        List<Node> neighbors = nodeOutputs. getOrDefault(current, List. of());
        for (Node neighbor : neighbors) {
            Long neighborId = neighbor.getId();

            if (!visited.contains(neighborId)) {
                parent.put(neighborId, current.getId());
                Long cycleNode = detectCycleDFS(
                        neighbor,
                        nodeOutputs,
                        visited,
                        recursionStack,
                        parent
                );
                if (cycleNode != null) {
                    return cycleNode;
                }
            } else if (recursionStack.contains(neighborId)) {
                parent.put(neighborId, current. getId());
                return neighborId;
            }
        }

        recursionStack.remove(current. getId());
        return null;
    }

    /**
     * Builds the cycle path for error reporting
     */
    private List<Long> buildCyclePath(Long cycleStart, Map<Long, Long> parent) {
        List<Long> path = new ArrayList<>();
        Long current = cycleStart;

        do {
            path.add(current);
            current = parent.get(current);
        } while (current != null && ! current.equals(cycleStart));

        path.add(cycleStart);
        Collections.reverse(path);
        return path;
    }

    /**
     * Validates the entire graph integrity
     */
    public void validateGraphIntegrity(CreateGraphRequest createGraphRequest) {
        List<Node> nodes = GraphBuilder.setupReferences(createGraphRequest.getNodes());
        Map<Node, List<Node>> nodeOutputs = GraphBuilder. mapOutputNodes(nodes);
        Map<Long, Node> nodeMap = GraphBuilder.getNodeMap(nodes);

        log.debug("Starting graph validation...");

        validateNoDuplicateIds(nodes);
        log.debug("No duplicate IDs found");

        validateReferences(nodes, nodeMap);
        log.debug("All node references are valid");

        validateNoCycles(nodes, nodeOutputs);
        log.debug("No cycles detected");

        log.info("Graph validation passed successfully");
    }
}
