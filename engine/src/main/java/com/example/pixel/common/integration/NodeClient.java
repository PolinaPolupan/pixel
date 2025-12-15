package com.example.pixel.common.integration;

import com.example.pixel.node_execution.dto.NodeClientData;
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
    private final static String REQUEST_FAILED_MESSAGE = "Request to {} failed with status {}: {}";
    private final static String CONNECTION_FAILED_MESSAGE = "Connection issue with service {}: {}";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${node.service.url}")
    private String nodeBaseUrl;

    @Value("${node.service.validate}")
    private String validationEndpoint;

    @Value("${node.service.execute}")
    private String executionEndpoint;

    @Value("${node.service.load_nodes}")
    private String loadNodesEndpoint;

    @Value("${node.service.load_graphs}")
    private String loadGraphsEndpoint;

    public void loadGraphs() {
        post(loadGraphsEndpoint, null, Object.class);
    }

    public void loadNodes() {
        post(loadNodesEndpoint, null, Object.class);
    }

    public NodeValidationResponse validate(NodeClientData nodeClientData) {
        return post(validationEndpoint, nodeClientData, NodeValidationResponse.class);
    }

    public NodeExecutionResponse execute(NodeClientData nodeClientData) {
        return post(executionEndpoint, nodeClientData, NodeExecutionResponse.class);
    }

    private <T> T post(String endpoint, Object requestBody, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.postForEntity(nodeBaseUrl + endpoint, requestBody, responseType);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error(REQUEST_FAILED_MESSAGE, nodeBaseUrl + endpoint, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (ResourceAccessException e) {
            log.error(CONNECTION_FAILED_MESSAGE, nodeBaseUrl, e.getMessage());
            throw e;
        }
    }
}