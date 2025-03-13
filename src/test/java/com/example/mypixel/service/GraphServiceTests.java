package com.example.mypixel.service;


import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Autowired
    private GraphService graphService;

    @Mock
    private Resource resource;


    @Test
    public void testProcessInputNode() {
        Resource mockResource = mock(Resource.class);
        Node inputNode = new Node("InputNode", new HashMap<>() {{
            put("filename", "input.jpg");
        }});

        when(tempStorageService.loadAsResource("input.jpg")).thenReturn(mockResource);
        when(storageService.loadAsResource("input.jpg")).thenReturn(mockResource);
        when(mockResource.getFilename()).thenReturn("input.jpg");

        graphService.processInputNode(inputNode);

        verify(tempStorageService, times(1)).createTempFileFromResource(eq(mockResource));
    }

    @Test
    public void testProcessNullInputNode() {
        Node inputNode = new Node("InputNode", new HashMap<>() {});

        graphService.processInputNode(inputNode);

        verify(tempStorageService, never()).createTempFileFromResource(any(Resource.class));
    }

    @Test
    public void testProcessEmptyGaussianBlurNode() {
        Node node = new Node("GaussianBlurNode", new HashMap<>() {});

        graphService.processGaussianBlurNode(node);

        verify(tempStorageService, never()).createTempFileFromFilename(anyString());
    }

    @Test
    public void testProcessGaussianBlurNode() {
        Map<String, Object> params = new HashMap<>();
        params.put("sizeX", 5);
        params.put("sizeY", 5);
        params.put("sigmaX", 1.0);
        params.put("sigmaY", 1.0);
        Node node = new Node("GaussianBlurNode", params);

        when(tempStorageService.createTempFileFromFilename(null)).thenReturn("tempFile.txt");

        graphService.processGaussianBlurNode(node);

        verify(filteringService, times(1)).gaussianBlur("tempFile.txt", 5, 5, 1.0, 1.0);
    }

    @Test
    public void testProcessGaussianBlurNodeWithNoParameters() {
        Node node = new Node("GaussianBlurNode", new HashMap<>() {});

        when(tempStorageService.createTempFileFromFilename(null)).thenReturn("tempFile.txt");

        graphService.processGaussianBlurNode(node);

        verify(filteringService, times(1)).gaussianBlur("tempFile.txt", 1, 1, 0.0, 0.0);
    }

    @Test
    public void testProcessOutputNode() {
        Node node = new Node("OutputNode", new HashMap<>() {{ put("filename", "output.jpeg"); }});

        when(tempStorageService.loadAsResource(null)).thenReturn(resource);
        graphService.processOutputNode(node);

      //  verify(storageService, times(1)).store(any(Resource.class), anyString());
      //  verify(tempStorageService, times(1)).createTempFileFromResource(any(Resource.class));
    }

    @Test
    public void testProcessGraphWithInvalidNodeType() {
        Map<String, Object> invalidParams = new HashMap<>();
        Node invalidNode = new Node("InvalidNode", invalidParams);

        List<Node> nodes = List.of(invalidNode);
        Graph graph = new Graph(nodes);

        assertThrows(InvalidNodeType.class, () -> graphService.processGraph(graph));

        verify(storageService, never()).loadAsResource(any());
        verify(storageService, never()).store(any(), any());
    }
}
