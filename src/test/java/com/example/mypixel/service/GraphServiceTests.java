package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class GraphServiceTests {

    @MockitoBean
    @Qualifier("storageService")
    private StorageService storageService;

    @MockitoBean
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @MockitoBean
    private FilteringService filteringService;

    @MockitoBean
    private NodeProcessorService nodeProcessorService;

    @Autowired
    private GraphService graphService;

    @Test
    void shouldProcessGraphWithSingleInputNode() {
        Node inputNode = new Node(0L,"InputNode", new HashMap<>() {{
            put("filename", "input1.jpg");
        }}, new ArrayList<>());

        Graph graph = new Graph();
        graph.setNodes(List.of(inputNode));

        graphService.processGraph(graph);

        verify(tempStorageService).init();
        verify(nodeProcessorService).processInputNode(inputNode);
        verify(tempStorageService, times(2)).deleteAll();
    }

    @Test
    void shouldProcessGraphWithMultipleInputNodes() {
        Node inputNode1 = new Node(0L,"InputNode", new HashMap<>() {{
            put("filename", "input1.jpg");
        }}, new ArrayList<>());

        Node inputNode2 = new Node(1L,"InputNode", new HashMap<>() {{
            put("filename", "input2.jpg");
        }}, new ArrayList<>());

        Graph graph = new Graph();
        graph.setNodes(Arrays.asList(inputNode1, inputNode2));

        graphService.processGraph(graph);

        verify(nodeProcessorService).processInputNode(inputNode1);
        verify(nodeProcessorService).processInputNode(inputNode2);
    }

    @Test
    void shouldThrowExceptionForInvalidNodeType() {
        Node inputNode1 = new Node(0L,"InvalidNode", new HashMap<>() {{
            put("filename", "input1.jpg");
        }}, new ArrayList<>());

        Node inputNode2 = new Node(1L,"InvalidNode", new HashMap<>() {{
            put("filename", "input2.jpg");
        }}, new ArrayList<>());

        Graph graph = new Graph();
        graph.setNodes(Arrays.asList(inputNode1, inputNode2));

        assertThrows(InvalidNodeType.class, () -> graphService.processGraph(graph));
    }

    @Test
    public void shouldProcessMultipleNodes() {
        Node inputNode = new Node(0L,"InputNode", new HashMap<>() {{
            put("filename", "input1.jpg");
        }}, List.of(1L, 2L, 7L));

        Node blurNode1 = new Node(1L,"GaussianBlurNode", new HashMap<>() {}, List.of(3L));
        Node blurNode2 = new Node(2L,"GaussianBlurNode", new HashMap<>() {}, List.of(3L));

        Node inputNode2 = new Node(6L,"InputNode", new HashMap<>() {{
            put("filename", "input1.jpg");
        }}, List.of(3L));

        Node blurNode3 = new Node(3L,"GaussianBlurNode", new HashMap<>() {}, List.of(4L, 5L));

        Node outputNode1 = new Node(4L,"OutputNode", new HashMap<>() {}, List.of());
        Node outputNode2 = new Node(5L,"OutputNode", new HashMap<>() {}, List.of());
        Node outputNode3 = new Node(7L,"OutputNode", new HashMap<>() {}, List.of());

        Graph graph = new Graph();
        graph.setNodes(Arrays.asList(inputNode, blurNode1, blurNode2, blurNode3, outputNode1, outputNode2, inputNode2, outputNode3));

        when(nodeProcessorService.processInputNode(inputNode)).thenReturn("file1.jpeg");
        when(nodeProcessorService.processGaussianBlurNode(blurNode1, "file1.jpeg")).thenReturn("file2.jpeg");
        when(nodeProcessorService.processGaussianBlurNode(blurNode2, "file2.jpeg")).thenReturn("file3.jpeg");

        graphService.processGraph(graph);

        InOrder inOrder = inOrder(nodeProcessorService);
        inOrder.verify(nodeProcessorService).processInputNode(inputNode);
        inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode1, "file1.jpeg");
        inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode2, "file2.jpeg");
      //  inOrder.verify(nodeProcessorService).processOutputNode(outputNode3, null);
       // inOrder.verify(nodeProcessorService).processGaussianBlurNode(blurNode3, null);
    }
}
