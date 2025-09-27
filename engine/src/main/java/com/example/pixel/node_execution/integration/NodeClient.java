package com.example.pixel.node_execution.integration;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node.dto.NodeClientData;
import com.example.pixel.node_execution.dto.NodeExecutionResponse;
import com.example.pixel.node_execution.dto.NodeValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NodeClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${node.service.url}")
    private String nodeBaseUrl;

    public NodeValidationResponse validateNode(NodeClientData nodeClientData) {
        return executeNodeRequest("/validate", nodeClientData, NodeValidationResponse.class);
    }

    public NodeExecutionResponse executeNode(NodeClientData nodeClientData) {
        return executeNodeRequest("/exec", nodeClientData, NodeExecutionResponse.class);
    }

    private <T> T executeNodeRequest(String endpoint, NodeClientData nodeClientData, Class<T> responseType) {
        try {
            log.debug("Executing request to node service: {}", endpoint);
            ResponseEntity<T> response = restTemplate.postForEntity(nodeBaseUrl + endpoint, nodeClientData, responseType);
            return response.getBody();

        } catch (HttpStatusCodeException e) {
            log.error("Node execution with id: {} type: {} failed with status {}: {}",
                    nodeClientData.getMeta().getNodeId(), nodeClientData.getMeta().getType(), e.getStatusCode(), e.getResponseBodyAsString());

            throw new NodeExecutionException(
                    "Node execution with id: " + nodeClientData.getMeta().getNodeId() +  " type: "
                            + nodeClientData.getMeta().getType() + " failed: " + e.getResponseBodyAsString(), e);

        } catch (ResourceAccessException e) {
            log.error("Connection issue with node service: {}", e.getMessage());
            throw new NodeExecutionException("Failed to connect to node service", e);
        }
    }
}