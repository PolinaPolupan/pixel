package com.example.mypixel.service;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class PythonNodeTester {

    private final RestTemplate restTemplate;
    private final String pythonServiceUrl;

    public PythonNodeTester(RestTemplate restTemplate, String pythonServiceUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceUrl = pythonServiceUrl;
    }

    public String sendJsonToPython(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(pythonServiceUrl, entity, String.class);
        return response.getBody();
    }
}
