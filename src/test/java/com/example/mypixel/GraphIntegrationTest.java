package com.example.mypixel;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.model.NodeType;
import com.example.mypixel.service.NodeProcessorService;
import com.example.mypixel.service.StorageService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GraphIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private StorageService tempStorageService;

    @MockitoBean
    private NodeProcessorService nodeProcessorService;

    private String baseUrl;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/v1/graph";
    }

    @Test
    public void testProcessGraph_Success() {
        Node inputNode = new Node(1L, NodeType.INPUT, Map.of("filename", "test.jpg"), List.of(2L));
        Node blurNode = new Node(2L, NodeType.GAUSSIAN_BLUR, Map.of("radius", 5), List.of(3L));
        Node outputNode = new Node(3L, NodeType.OUTPUT, Map.of("prefix", "output"), List.of());

        Graph graph = new Graph(List.of(inputNode, blurNode, outputNode));

        when(nodeProcessorService.processInputNode(any())).thenReturn("temp_input.jpg");
        when(nodeProcessorService.processGaussianBlurNode(any(), any())).thenReturn("temp_blur.jpg");
        when(nodeProcessorService.processOutputNode(any(), any(), eq("test.jpg"))).thenReturn("final_output.jpg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Graph> requestEntity = new HttpEntity<>(graph, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl, requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(tempStorageService).init();
        verify(tempStorageService, times(2)).deleteAll();
        verify(nodeProcessorService).processInputNode(any());
        verify(nodeProcessorService).processGaussianBlurNode(any(), eq("temp_input.jpg"));
        verify(nodeProcessorService).processOutputNode(any(), eq("temp_blur.jpg"), eq("test.jpg"));
    }

    @Test
    public void testProcessGraph_ComplexGraph() {
        Node inputNode = new Node(1L, NodeType.INPUT, Map.of("filename", "test.jpg"), List.of(2L, 3L));
        Node blurNode1 = new Node(2L, NodeType.GAUSSIAN_BLUR, Map.of(), List.of(4L));
        Node blurNode2 = new Node(3L, NodeType.GAUSSIAN_BLUR, Map.of(), List.of(4L));
        Node outputNode = new Node(4L, NodeType.OUTPUT, Map.of("prefix", "output"), List.of());

        Graph graph = new Graph(List.of(inputNode, blurNode1, blurNode2, outputNode));

        when(nodeProcessorService.processInputNode(any())).thenReturn("temp_input.jpg");
        when(nodeProcessorService.processGaussianBlurNode(any(), any())).thenReturn("temp_blur.jpg");
        when(nodeProcessorService.processOutputNode(any(), any(), eq("temp_input.jpg"))).thenReturn("output.jpg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Graph> requestEntity = new HttpEntity<>(graph, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl, requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(nodeProcessorService).processInputNode(any());
        verify(nodeProcessorService).processGaussianBlurNode(eq(blurNode1), any());
        verify(nodeProcessorService).processGaussianBlurNode(eq(blurNode2), any());
        verify(nodeProcessorService, times(2)).processOutputNode(any(), any(), eq("test.jpg"));
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
        String invalidGraphJson = "{\"nodes\":[{\"id\":1,\"type\":\"UnsupportedType\",\"params\":{},\"outputs\":[]}]}";

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
