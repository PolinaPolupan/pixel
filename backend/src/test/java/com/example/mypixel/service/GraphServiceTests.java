package com.example.mypixel.service;

import com.example.mypixel.NodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.NodeReference;
import com.example.mypixel.model.node.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@SpringBootTest
public class GraphServiceTests {

    @MockitoBean
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @MockitoBean
    private NodeProcessorService nodeProcessorService;

    @Autowired
    private GraphService graphService;

    @MockitoBean
    private StorageService storageService;

    @Test
    void shouldProcessGraphWithSingleInputNode() {
        Resource mockResource = mock(Resource.class);
        Node inputNode = new InputNode(0L, NodeType.INPUT.getName(), Map.of("files", List.of("input1.jpg")));

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
        Node inputNode1 = new InputNode(0L, NodeType.INPUT.getName(), Map.of("files", List.of("input1.jpg")));
        Node inputNode2 = new InputNode(1L, NodeType.INPUT.getName(), Map.of("files", List.of("input2.jpg")));

        Graph graph = new Graph(List.of(inputNode1, inputNode2));

        when(storageService.loadAsResource("input1.jpg")).thenReturn(mockResource);
        when(tempStorageService.createTempFileFromResource(mockResource)).thenReturn("input1.jpg");
        when(storageService.loadAsResource("input2.jpg")).thenReturn(mockResource);
        when(tempStorageService.createTempFileFromResource(mockResource)).thenReturn("input2.jpg");

        graphService.processGraph(graph);

        verify(nodeProcessorService).processNode(inputNode1);
        verify(nodeProcessorService).processNode(inputNode2);
    }

    @Test
    public void shouldProcessMultipleNodes() {
        Node inputNode = new InputNode(
                0L,
                NodeType.INPUT.getName(),
                Map.of("files", List.of("input1.jpeg")));

        Node blurNode1 = new GaussianBlurNode(
                1L,
                NodeType.GAUSSIAN_BLUR.getName(),
                Map.of(
                "files", new NodeReference("@node:0:files"),
                "sizeX", 5,
                "sizeY", 5,
                "sigmaX", 5,
                "sigmaY", 5)
        );

        Node blurNode2 = new GaussianBlurNode(
                2L,
                NodeType.GAUSSIAN_BLUR.getName(),
                Map.of(
                "files", new NodeReference("@node:0:files"),
                "sizeX", 5,
                "sizeY", 5,
                "sigmaX", 5,
                "sigmaY", 5)
        );

        Node inputNode2 = new InputNode(
                6L,
                NodeType.INPUT.getName(),
                Map.of("files", List.of("input2.jpeg"))
        );

        Node combine = new CombineNode(
                8L,
                "Combine",
                Map.of(
                "files_0", new NodeReference("@node:1:files"),
                "files_1", new NodeReference("@node:2:files"),
                "files_2", new NodeReference("@node:6:files"))
        );

        Node blurNode3 = new GaussianBlurNode(
                3L,
                NodeType.GAUSSIAN_BLUR.getName(),
                Map.of(
                "files", new NodeReference("@node:8:files"),
                "sizeX", 5,
                "sizeY", 5,
                "sigmaX", 5,
                "sigmaY", 5)
        );

        Node outputNode1 = new OutputNode(
                4L,
                NodeType.OUTPUT.getName(),
                Map.of("files", new NodeReference("@node:3:files")));

        Node outputNode2 = new OutputNode(
                5L,
                NodeType.OUTPUT.getName(),
                Map.of("files", new NodeReference("@node:3:files")));

        Node outputNode3 = new OutputNode(
                7L,
                NodeType.OUTPUT.getName(),
                Map.of("files", new NodeReference("@node:6:files")));

        Graph graph = new Graph(List.of(inputNode, combine, blurNode1, blurNode2, blurNode3, outputNode1, outputNode2, inputNode2, outputNode3));

        graphService.processGraph(graph);

        InOrder inOrder = inOrder(nodeProcessorService);

        inOrder.verify(nodeProcessorService).processNode(inputNode);
        inOrder.verify(nodeProcessorService).processNode(inputNode2);
        inOrder.verify(nodeProcessorService).processNode(blurNode1);
        inOrder.verify(nodeProcessorService).processNode(blurNode2);
        inOrder.verify(nodeProcessorService).processNode(outputNode3);
        inOrder.verify(nodeProcessorService).processNode(combine);
        inOrder.verify(nodeProcessorService).processNode(blurNode3);
        inOrder.verify(nodeProcessorService).processNode(outputNode1);
        inOrder.verify(nodeProcessorService).processNode(outputNode2);
    }
}
