package com.example.mypixel.service;


import com.example.mypixel.exception.InvalidNodeType;
import com.example.mypixel.model.Graph;
import com.example.mypixel.model.Node;
import com.example.mypixel.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import java.util.Arrays;
import java.util.HashMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GraphServiceTests {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private GraphService graphService;

    private Resource mockResource;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockResource = mock(Resource.class);
    }

    @Test
    public void testProcessValidGraph() {
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("filename", "input.jpg");

        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("filename", "output.jpg");

        Node inputNode = new Node("InputNode", inputParams);
        Node outputNode = new Node("OutputNode", outputParams);

        List<Node> nodes = Arrays.asList(inputNode, outputNode);
        Graph graph = new Graph(nodes);

        when(storageService.loadAsResource("input.jpg")).thenReturn(mockResource);

        graphService.processGraph(graph);

        verify(storageService).loadAsResource("input.jpg");
        verify(storageService).store(mockResource, "output.jpg");
    }

    @Test
    public void testProcessGraphWithInvalidNodeType() {
        Map<String, Object> invalidParams = new HashMap<>();
        Node invalidNode = new Node("InvalidNode", invalidParams);

        List<Node> nodes = List.of(invalidNode);
        Graph graph = new Graph(nodes);

        assertThrows(InvalidNodeType.class, () -> {
            graphService.processGraph(graph);
        });

        verify(storageService, never()).loadAsResource(any());
        verify(storageService, never()).store(any(), any());
    }
}
