package com.example.mypixel.service;


import com.example.mypixel.model.Node;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
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
        Node inputNode = new Node(0L,"InputNode", new HashMap<>() {{
            put("filename", "input.jpg");
        }}, new ArrayList<>());

        when(tempStorageService.loadAsResource("input.jpg")).thenReturn(mockResource);
        when(storageService.loadAsResource("input.jpg")).thenReturn(mockResource);
        when(mockResource.getFilename()).thenReturn("input.jpg");

        nodeProcessorService.processInputNode(inputNode);

        verify(tempStorageService, times(1)).createTempFileFromResource(eq(mockResource));
    }

    @Test
    public void testProcessNullInputNode() {
        Node inputNode = new Node(0L, "InputNode", new HashMap<>() {}, new ArrayList<>());

        nodeProcessorService.processInputNode(inputNode);

        verify(tempStorageService, never()).createTempFileFromResource(any(Resource.class));
    }

    @Test
    public void testProcessEmptyGaussianBlurNode() {
        Node node = new Node(0L, "GaussianBlurNode", new HashMap<>() {}, new ArrayList<>());

        nodeProcessorService.processGaussianBlurNode(node, null);

        verify(tempStorageService, never()).createTempFileFromFilename(anyString());
    }

    @Test
    public void testProcessGaussianBlurNode() {
        Map<String, Object> params = new HashMap<>();
        params.put("sizeX", 5);
        params.put("sizeY", 5);
        params.put("sigmaX", 1.0);
        params.put("sigmaY", 1.0);
        Node node = new Node(0L , "GaussianBlurNode", params, new ArrayList<>());

        when(tempStorageService.createTempFileFromFilename("tempFile.txt")).thenReturn("tempFile.txt");

        nodeProcessorService.processGaussianBlurNode(node, "tempFile.txt");

        verify(filteringService, times(1)).gaussianBlur("tempFile.txt", 5, 5, 1.0, 1.0);
    }

    @Test
    public void testProcessGaussianBlurNodeWithNoParameters() {
        Node node = new Node(0L, "GaussianBlurNode", new HashMap<>() {}, new ArrayList<>());

        when(tempStorageService.createTempFileFromFilename("tempFile.txt")).thenReturn("tempFile.txt");

        nodeProcessorService.processGaussianBlurNode(node, "tempFile.txt");

        verify(filteringService, times(1)).gaussianBlur("tempFile.txt", 1, 1, 0.0, 0.0);
    }

    @Test
    public void testProcessOutputNode() {
        Node node = new Node(0L, "OutputNode", new HashMap<>() {{ put("filename", "output.jpeg"); }}, new ArrayList<>());

        when(tempStorageService.loadAsResource(null)).thenReturn(resource);
        nodeProcessorService.processOutputNode(node, null);

        verify(storageService, times(1)).store(any(Resource.class), anyString());
        verify(tempStorageService, times(1)).createTempFileFromResource(any(Resource.class));
    }
}
