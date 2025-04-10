package com.example.mypixel.service;


import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.NodeReference;
import com.example.mypixel.model.node.GaussianBlurNode;
import com.example.mypixel.model.node.InputNode;
import com.example.mypixel.model.node.Node;
import com.example.mypixel.NodeType;
import com.example.mypixel.model.node.OutputNode;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NodeProcessorServiceTests {

    @MockitoBean
    @Qualifier("storageService")
    private StorageService storageService;

    @MockitoBean
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @MockitoBean
    private FilteringService filteringService;

    @Autowired
    private NodeProcessorService nodeProcessorService;

    @Mock
    private Resource resource;


    @Test
    public void testProcessInputNode() {
        Resource mockResource = mock(Resource.class);
        Node inputNode = new InputNode(0L, NodeType.INPUT.getName(), Map.of("files", List.of("input.jpg")));

        when(tempStorageService.loadAsResource("input.jpg")).thenReturn(mockResource);
        when(storageService.loadAsResource("input.jpg")).thenReturn(mockResource);
        when(mockResource.getFilename()).thenReturn("input.jpg");

        nodeProcessorService.processNode(inputNode);

        verify(tempStorageService, times(1)).createTempFileFromResource(eq(mockResource));
    }

    @Test
    public void testProcessNullInputNode() {
        Node inputNode = new InputNode(0L, NodeType.INPUT.getName(), new HashMap<>() {});

        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(inputNode));
    }

    @Test
    public void testProcessEmptyGaussianBlurNode() {
        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
                "files", List.of(),
                "sizeX", 5,
                "sizeY", 5,
                "sigmaX", 5,
                "sigmaY", 5));

        nodeProcessorService.processNode(node);

        verify(tempStorageService, never()).createTempFileFromResource(any());
    }

    @Test
    public void testProcessGaussianBlurNode() {
        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
                "files", List.of("input.jpeg"),
                "sizeX", 5,
                "sizeY", 5,
                "sigmaX", 5.0,
                "sigmaY", 5.0));

        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpeg");

        nodeProcessorService.processNode(node);

        verify(filteringService, times(1))
                .gaussianBlur("input.jpeg", 5, 5, 5.0, 5.0);
    }

    @Test
    public void testProcessGaussianBlurNodeWithNoParameters() {
        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), Map.of(
                "files", List.of("input.jpeg"), "sizeX", 5));

        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpeg");

        nodeProcessorService.processNode(node);

        verify(filteringService, times(1))
                .gaussianBlur("input.jpeg", 5, 5, 0.0, 0.0);
    }

    @Test
    public void testProcessOutputNode() {
        Node node = new OutputNode(0L, NodeType.OUTPUT.getName(), Map.of(
                "files", List.of("input.jpeg"),
                "prefix", "output"
        ));

        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
        when(tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource("input.jpeg"))).thenReturn("input.jpeg");
        when(tempStorageService.removeExistingPrefix("input.jpeg")).thenReturn("input.jpeg");
        nodeProcessorService.processNode(node);

        verify(storageService, times(1)).store(eq(resource), eq("output_input.jpeg"));
    }

    @Test
    public void testProcessOutputNodeWithoutPrefix() {
        Node node = new OutputNode(0L, NodeType.OUTPUT.getName(), Map.of("files", List.of("input.jpeg")));

        when(tempStorageService.loadAsResource("input.jpeg")).thenReturn(resource);
        when(tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource("input.jpeg"))).thenReturn("input.jpeg");
        when(tempStorageService.removeExistingPrefix("input.jpeg")).thenReturn("input.jpeg");
        nodeProcessorService.processNode(node);

        verify(storageService, times(1)).store(eq(resource), eq("input.jpeg"));
    }

    @Test
    public void testProcessNodeWithInvalidNodeReference() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("files", new NodeReference("@node:999:files"));
        inputs.put("sizeX", 5);
        inputs.put("sizeY", 5);
        inputs.put("sigmaX", 5.0);
        inputs.put("sigmaY", 5.0);

        Node node = new GaussianBlurNode(1L, NodeType.GAUSSIAN_BLUR.getName(), inputs);

        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
    }

    @Test
    public void testProcessNodeWithInvalidOutputReference() {
        Node inputNode = new InputNode(0L, NodeType.INPUT.getName(), Map.of("files", List.of("input.jpg")));
        when(storageService.loadAsResource("input.jpg")).thenReturn(resource);
        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpg");
        nodeProcessorService.processNode(inputNode);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("files", new NodeReference("@node:0:nonexistentOutput"));
        inputs.put("sizeX", 5);
        inputs.put("sizeY", 5);
        inputs.put("sigmaX", 5.0);
        inputs.put("sigmaY", 5.0);

        Node blurNode = new GaussianBlurNode(1L, NodeType.GAUSSIAN_BLUR.getName(), inputs);

        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(blurNode));
    }

    @Test
    public void testProcessNodeWithInvalidInputType() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("files", List.of("input.jpg"));
        inputs.put("sizeX", "not-a-number");
        inputs.put("sizeY", 5);
        inputs.put("sigmaX", 5.0);
        inputs.put("sigmaY", 5.0);

        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), inputs);
        when(tempStorageService.loadAsResource("input.jpg")).thenReturn(resource);
        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpg");

        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
    }

    @Test
    public void testProcessNodeWithMissingRequiredInput() {
        Map<String, Object> inputs = new HashMap<>();

        inputs.put("sizeX", 5);
        inputs.put("sizeY", 5);

        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), inputs);

        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
    }

    @Test
    public void testProcessNodeWithNullInputType() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("files", null);
        inputs.put("sizeX", 5);
        inputs.put("sizeY", 5);
        inputs.put("sigmaX", 5.0);
        inputs.put("sigmaY", 5.0);

        Node node = new GaussianBlurNode(0L, NodeType.GAUSSIAN_BLUR.getName(), inputs);
        when(tempStorageService.loadAsResource("input.jpg")).thenReturn(resource);
        when(tempStorageService.createTempFileFromResource(resource)).thenReturn("input.jpg");

        assertThrows(InvalidNodeParameter.class, () -> nodeProcessorService.processNode(node));
    }
}
