package com.example.pixel.node_execution.integration;

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
    private final static String REQUEST_FAILED_MESSAGE = "Request to %s failed with status %s: %s";
    private final static String CONNECTION_FAILED_MESSAGE = "Connection issue with service %s: %s";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${node.service.url}")
    private String nodeBaseUrl;

    @Value("${node.service.validate}")
    private String validationEndpoint;

    @Value("${node.service.execute}")
    private String executionEndpoint;

    public NodeValidationResponse validate(NodeClientData nodeClientData) {
        return post(validationEndpoint, nodeClientData, NodeValidationResponse.class);
    }

    public NodeExecutionResponse execute(NodeClientData nodeClientData) {
        return post(executionEndpoint, nodeClientData, NodeExecutionResponse.class);
    }

    private <T> T post(String endpoint, NodeClientData nodeClientData, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.postForEntity(nodeBaseUrl + endpoint, nodeClientData, responseType);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(String.format(
                    REQUEST_FAILED_MESSAGE, nodeBaseUrl + endpoint, e.getStatusCode(), e.getResponseBodyAsString()
            ), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException(String.format(CONNECTION_FAILED_MESSAGE, nodeBaseUrl, e.getMessage()), e);
        }
    }
}