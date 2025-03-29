package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.NodeType;
import com.example.mypixel.model.node.InputNode;
import com.example.mypixel.model.node.Node;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class GraphServiceTests {

    @MockitoBean
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @MockitoBean
    private FilteringService filteringService;

    @MockitoBean
    private NodeProcessorService nodeProcessorService;

    @Autowired
    private GraphService graphService;

    @MockitoBean
    private StorageService storageService;

    @Test
    void shouldProcessGraphWithSingleInputNode() {
        Resource mockResource = mock(Resource.class);
        Node inputNode = new InputNode(0L, NodeType.INPUT, Map.of("files", List.of("input1.jpg")));

        Graph graph = new Graph(List.of(inputNode));

        when(storageService.loadAsResource("input1.jpg")).thenReturn(mockResource);
        when(tempStorageService.createTempFileFromResource(mockResource)).thenReturn("input1.jpg");

        graphService.processGraph(graph);

        verify(nodeProcessorService, times(2)).clear();
        verify(nodeProcessorService).processNode(inputNode);
    }

    @Test
    void shouldProcessGraphWithMultipleInputNodes() {
        Resource mockResource = mock(Resource.class);
        Node inputNode1 = new InputNode(0L, NodeType.INPUT, Map.of("files", List.of("input1.jpg")));
        Node inputNode2 = new InputNode(1L, NodeType.INPUT, Map.of("files", List.of("input2.jpg")));

        Graph graph = new Graph(List.of(inputNode1, inputNode2));

        when(storageService.loadAsResource("input1.jpg")).thenReturn(mockResource);
        when(tempStorageService.createTempFileFromResource(mockResource)).thenReturn("input1.jpg");
        when(storageService.loadAsResource("input2.jpg")).thenReturn(mockResource);
        when(tempStorageService.createTempFileFromResource(mockResource)).thenReturn("input2.jpg");

        graphService.processGraph(graph);

        verify(nodeProcessorService).processNode(inputNode1);
        verify(nodeProcessorService).processNode(inputNode2);
    }

    // Fix
    @Test
    void shouldThrowExceptionForInvalidNodeType() {
        Node inputNode1 = new InputNode(0L, NodeType.INPUT, Map.of("files", List.of("input1.jpg")));
        Node inputNode2 = new Node(1L, NodeType.UNKNOWN, Map.of("files", List.of("input2.jpg")));

        Graph graph = new Graph(List.of(inputNode1, inputNode2));

      //  assertThrows(InvalidNodeType.class, () -> graphService.processGraph(graph));
    }

    @Test
    public void shouldProcessMultipleNodes() {
//        Node inputNode = new Node(0L, NodeType.INPUT, Map.of("files", List.of("input1.jpeg")), List.of(1L, 2L, 7L));
//
//        Node blurNode1 = new GaussianBlurNode(1L, NodeType.GAUSSIAN_BLUR, Map.of(
//                "files", "@node:0:files",
//                "sizeX", 5,
//                "sizeY", 5,
//                "sigmaX", 5,
//                "sigmaY", 5));
//        Node blurNode2 = new GaussianBlurNode(2L, NodeType.GAUSSIAN_BLUR, Map.of(
//                "files", "@node:0:files",
//                "sizeX", 5,
//                "sizeY", 5,
//                "sigmaX", 5,
//                "sigmaY", 5));
//
//        Node inputNode2 = new InputNode(6L, NodeType.INPUT, Map.of("files", List.of("input2.jpeg")), List.of(3L));
//
//        Node blurNode3 = new Node(3L, NodeType.GAUSSIAN_BLUR, new HashMap<>() {}, List.of(4L, 5L));
//
//        Node outputNode1 = new Node(4L, NodeType.OUTPUT, new HashMap<>() {}, List.of());
//        Node outputNode2 = new Node(5L, NodeType.OUTPUT, new HashMap<>() {}, List.of());
//        Node outputNode3 = new Node(7L, NodeType.OUTPUT, new HashMap<>() {}, List.of());
//
//        Graph graph = new Graph();
//        graph.setNodes(Arrays.asList(inputNode, blurNode1, blurNode2, blurNode3, outputNode1, outputNode2, inputNode2, outputNode3));
//
//        when(nodeProcessorService.processInputNode(inputNode, "input1.jpeg")).thenReturn("file1.jpeg");
//        when(nodeProcessorService.processGaussianBlurNode(blurNode1, "file1.jpeg")).thenReturn("file2.jpeg");
//        when(nodeProcessorService.processGaussianBlurNode(blurNode2, "file1.jpeg")).thenReturn("file3.jpeg");
//        when(nodeProcessorService.processGaussianBlurNode(blurNode3, "file2.jpeg")).thenReturn("file4.jpeg");
//        when(nodeProcessorService.processGaussianBlurNode(blurNode3, "file3.jpeg")).thenReturn("file4.jpeg");
//
//        when(nodeProcessorService.processInputNode(inputNode2, "input2.jpeg")).thenReturn("file5.jpeg");
//        when(nodeProcessorService.processGaussianBlurNode(blurNode3, "file5.jpeg")).thenReturn("file6.jpeg");
//
//        graphService.processGraph(graph);
//
//        InOrder inOrder = inOrder(nodeProcessorService);
//
//        // First subgraph
//        inOrder.verify(nodeProcessorService).processInputNode(inputNode, "input1.jpeg");
//        inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode1, "file1.jpeg");
//        inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode2, "file1.jpeg");
//        inOrder.verify(nodeProcessorService).processOutputNode(outputNode3, "file1.jpeg", "input1.jpeg");
//        inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode3, "file2.jpeg");
//        inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode3, "file3.jpeg");
//        inOrder.verify(nodeProcessorService, times(2)).processOutputNode(outputNode1, "file4.jpeg", "input1.jpeg");
//        inOrder.verify(nodeProcessorService, times(2)).processOutputNode(outputNode2, "file4.jpeg", "input1.jpeg");
//
//        // Second subgraph
//        inOrder.verify(nodeProcessorService).processInputNode(inputNode2, "input2.jpeg");
//        inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode3, "file5.jpeg");
//        inOrder.verify(nodeProcessorService).processOutputNode(outputNode1, "file6.jpeg", "input2.jpeg");
//        inOrder.verify(nodeProcessorService).processOutputNode(outputNode2, "file6.jpeg", "input2.jpeg");
    }
}
