package com.example.mypixel;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.node.GaussianBlurNode;
import com.example.mypixel.model.node.InputNode;
import com.example.mypixel.model.node.Node;
import com.example.mypixel.model.node.OutputNode;
import com.example.mypixel.service.NodeProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GraphIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private NodeProcessorService nodeProcessorService;

    private String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/v1/graph";
    }

    @Test
    public void testProcessGraph_Success() {
        Node inputNode = new InputNode(1L, NodeType.INPUT.getName(), Map.of("files", List.of("test.jpg")));
        Node blurNode = new GaussianBlurNode(2L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
                "files", "@node:1:files",
                "sizeX", 5,
                "sizeY", 5,
                "sigmaX", 5,
                "sigmaY", 5));
        Node outputNode = new OutputNode(3L, NodeType.OUTPUT.getName(), Map.of("files" , "@node:2:files", "prefix", "output"));

        Graph graph = new Graph(List.of(inputNode, blurNode, outputNode));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Graph> requestEntity = new HttpEntity<>(graph, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl, requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testProcessGraph_EmptyGraph() {
        Graph emptyGraph = new Graph(List.of());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Graph> requestEntity = new HttpEntity<>(emptyGraph, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl, requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testProcessGraph_InvalidNodeType() {
        String invalidGraphJson = "{\"nodes\":[{\"id\":1,\"type\":\"UnsupportedType\",\"inputs\":{},\"outputs\":[]}]}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(invalidGraphJson, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl, requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testProcessGraph_InvalidJson() {
        String malformedJson = "{nodes: [invalid]}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(malformedJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
