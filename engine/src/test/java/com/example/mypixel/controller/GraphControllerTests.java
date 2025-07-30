//package com.example.mypixel.controller;
//
//import com.example.mypixel.config.MyPixelConfig;
//import com.example.mypixel.model.Graph;
//import com.example.mypixel.model.node.GaussianBlurNode;
//import com.example.mypixel.model.node.InputNode;
//import com.example.mypixel.model.node.Node;
//import com.example.mypixel.NodeType;
//import com.example.mypixel.model.node.OutputNode;
//import com.example.mypixel.service.GraphService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//@WebMvcTest(GraphController.class)
//@Import(MyPixelConfig.class)
//public class GraphControllerTests {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private GraphService graphService;
//
//    @MockitoBean
//    private FileManager fileManager;
//
//    @Autowired
//    private ObjectMapper graphObjectMapper;
//
//    private String baseRoute;
//    private String sceneId;
//
//    @BeforeEach
//    public void setUp() {
//        sceneId = "test-scene-" + UUID.randomUUID();
//
//        doNothing().when(fileManager).createScene(sceneId);
//
//        when(fileManager.sceneExists(sceneId)).thenReturn(true);
//
//        baseRoute = "/v1/scene/" + sceneId + "/graph";
//
//        fileManager.createScene(sceneId);
//    }
//
//    @Test
//    public void testProcessGraph_Success() throws Exception {
//        Node inputNode = new InputNode(1L, NodeType.INPUT.getName(), Map.of("files", List.of("test.jpg")));
//        Node blurNode = new GaussianBlurNode(2L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
//                "files", "@node:1:files",
//                "sizeX", 5,
//                "sizeY", 5,
//                "sigmaX", 5,
//                "sigmaY", 5));
//        Node outputNode = new OutputNode(3L, NodeType.OUTPUT.getName(), Map.of("files", "@node:2:files"));
//
//        Graph graph = new Graph(List.of(inputNode, blurNode, outputNode));
//
//        doNothing().when(graphService).processGraph(any(Graph.class), eq(sceneId));
//
//        mockMvc.perform(post(baseRoute)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(graphObjectMapper.writeValueAsString(graph)))
//                .andExpect(status().isOk());
//
//        verify(graphService).processGraph(any(Graph.class), eq(sceneId));
//    }
//
//    @Test
//    public void testProcessGraph_EmptyGraph() throws Exception {
//        Graph emptyGraph = new Graph(List.of());
//
//        doNothing().when(graphService).processGraph(any(Graph.class), eq(sceneId));
//
//        mockMvc.perform(post(baseRoute)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(graphObjectMapper.writeValueAsString(emptyGraph)))
//                .andExpect(status().isOk());
//
//        verify(graphService).processGraph(any(Graph.class), eq(sceneId));
//    }
//
//    @Test
//    public void testProcessGraph_InvalidJson() throws Exception {
//        String invalidJson = "{\"nodes\": invalid}";
//
//        mockMvc.perform(post(baseRoute)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(invalidJson))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void testProcessGraph_SceneDoesNotExist() throws Exception {
//        String nonExistentSceneId = "non-existent-scene";
//        String nonExistentSceneRoute = "/v1/scene/" + nonExistentSceneId + "/graph";
//
//        Node inputNode = new InputNode(1L, NodeType.INPUT.getName(), Map.of("files", List.of("test.jpg")));
//        Graph graph = new Graph(List.of(inputNode));
//
//        when(fileManager.sceneExists(nonExistentSceneId)).thenReturn(false);
//
//        mockMvc.perform(post(nonExistentSceneRoute)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(graphObjectMapper.writeValueAsString(graph)))
//                .andExpect(status().isNotFound());
//
//        verify(graphService, never()).processGraph(any(Graph.class), eq(nonExistentSceneId));
//    }
//}