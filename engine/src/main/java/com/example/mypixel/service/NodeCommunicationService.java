package com.example.mypixel.service;

import com.example.mypixel.exception.NodeExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class NodeCommunicationService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${node.service.url}")
    private String nodeBaseUrl;

    public <T> T executeNodeRequest(String endpoint, Object payload, Class<T> responseType) {
        try {
            log.debug("Executing request to node service: {}", endpoint);

            ResponseEntity<T> response = restTemplate.postForEntity(
                    nodeBaseUrl + endpoint, payload, responseType);

            return response.getBody();

        } catch (HttpStatusCodeException e) {
            log.error("Node execution failed with status {}: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new NodeExecutionException(
                    "Node execution failed: " + e.getResponseBodyAsString(), e);

        } catch (ResourceAccessException e) {
            log.error("Connection issue with node service: {}", e.getMessage());
            throw new NodeExecutionException("Failed to connect to node service", e);
        }
    }
}