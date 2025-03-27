package com.example.mypixel.controller;

import com.example.mypixel.model.Graph;
import com.example.mypixel.model.node.Node;
import com.example.mypixel.model.NodeType;
import com.example.mypixel.service.GraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(GraphController.class)
public class GraphControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GraphService graphService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testProcessGraph_Success() throws Exception {
        Node inputNode = new Node(1L, NodeType.INPUT, Map.of("filename", "test.jpg"), List.of(2L));
        Node blurNode = new Node(2L, NodeType.GAUSSIAN_BLUR, Map.of("radius", 5), List.of(3L));
        Node outputNode = new Node(3L, NodeType.OUTPUT, Map.of("filename", "output.jpg"), List.of());

        Graph graph = new Graph(List.of(inputNode, blurNode, outputNode));

        doNothing().when(graphService).processGraph(any(Graph.class));

        mockMvc.perform(post("/v1/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(graph)))
                .andExpect(status().isOk());

        verify(graphService).processGraph(any(Graph.class));
    }

    @Test
    public void testProcessGraph_EmptyGraph() throws Exception {
        Graph emptyGraph = new Graph(List.of());

        doNothing().when(graphService).processGraph(any(Graph.class));

        mockMvc.perform(post("/v1/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyGraph)))
                .andExpect(status().isOk());

        verify(graphService).processGraph(any(Graph.class));
    }

    @Test
    public void testProcessGraph_InvalidJson() throws Exception {
        String invalidJson = "{\"nodes\": [invalid]}";

        mockMvc.perform(post("/v1/graph")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
