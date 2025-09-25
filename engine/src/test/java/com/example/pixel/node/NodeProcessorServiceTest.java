package com.example.pixel.node;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node.model.*;
import com.example.pixel.node.service.NodeCacheService;
import com.example.pixel.node.service.NodeClient;
import com.example.pixel.node.service.NodeProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NodeProcessorServiceTest {

    @Mock
    private NodeCacheService nodeCacheService;

    @Mock
    private Node node;

    @InjectMocks
    private NodeProcessorService nodeProcessorService;

    @Mock
    private NodeClient nodeClient;

    @Captor
    private ArgumentCaptor<Map<String, Object>> inputsCaptor;

    @Mock
    private NodeValidationResponse nodeValidationResponse;

    @Mock
    private NodeExecutionResponse nodeExecutionResponse;

    private final Long sceneId = 1L;
    private final Long taskId = 100L;
    private final Long nodeId = 10L;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;

    @BeforeEach
    void setUp() {
        inputs = new HashMap<>();
        outputs = new HashMap<>();

        when(node.getId()).thenReturn(nodeId);
        when(node.getType()).thenReturn("testNode");
        when(node.getInputs()).thenReturn(inputs);
        when(nodeExecutionResponse.getOutputs()).thenReturn(outputs);
    }

    @Test
    void processNodeInternal_shouldCacheInputsAndOutputs() {
        when(nodeClient.validateNode(any(NodeData.class))).thenReturn(nodeValidationResponse);
        when(nodeClient.executeNode(any(NodeData.class))).thenReturn(nodeExecutionResponse);

        nodeProcessorService.processNode(node,  sceneId, taskId);

        verify(nodeCacheService).put("100:10:input", inputs);
        verify(nodeCacheService).put("100:10:output", outputs);
    }

    @Test
    void resolveReference_shouldThrowWhenOutputDoesNotExist() {
        NodeReference reference = new NodeReference("@node:20:nonexistent");
        inputs.put("refParam", reference);

        Map<String, Object> referencedOutput = new HashMap<>();

        when(nodeCacheService.exists(taskId + ":" + nodeId + ":output")).thenReturn(true);
        when(nodeCacheService.get(taskId + ":" + nodeId + ":output")).thenReturn(referencedOutput);

        assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNode(node, sceneId, taskId));
    }

    @Test
    void resolveReference_shouldThrowWhenCacheKeyDoesNotExist() {
        NodeReference reference = new NodeReference("@node:20:result");
        inputs.put("refParam", reference);

        when(nodeCacheService.exists(taskId + ":" + nodeId + ":output")).thenReturn(false);

        assertThrows(NodeExecutionException.class, () ->
                nodeProcessorService.processNode(node, sceneId, taskId));
    }

    @Test
    void processNodeInternal_shouldHandleValidationFailure() {
        when(nodeClient.validateNode(any(NodeData.class))).thenThrow(NodeExecutionException.class);
        when(nodeClient.executeNode(any(NodeData.class))).thenReturn(nodeExecutionResponse);

        assertThrows(NodeExecutionException.class, () ->
                nodeProcessorService.processNode(node, sceneId, taskId));
    }

    @Test
    void processNodeInternal_shouldHandleExecutionFailure() {
        when(nodeClient.validateNode(any(NodeData.class))).thenReturn(nodeValidationResponse);
        when(nodeClient.executeNode(any(NodeData.class))).thenThrow(NodeExecutionException.class);

        assertThrows(NodeExecutionException.class, () ->
                nodeProcessorService.processNode(node, sceneId, taskId));

        verify(nodeCacheService).put(taskId + ":" + nodeId + ":input", inputs);
        verify(nodeCacheService, never()).put(eq(taskId + ":" + nodeId + ":output"), any());
    }

    @Test
    void resolveInputs_shouldHandleEmptyInputs() {
        inputs.clear();

        when(nodeClient.executeNode(any(NodeData.class))).thenReturn(nodeExecutionResponse);

        nodeProcessorService.processNode(node, sceneId, taskId);

        verify(node).setInputs(inputsCaptor.capture());
        Map<String, Object> resolvedInputs = inputsCaptor.getValue();

        assertTrue(resolvedInputs.isEmpty());
    }
}