package com.example.pixel.node;

import com.example.pixel.common.exception.NodeExecutionException;
import com.example.pixel.node_execution.dto.NodeClientData;
import com.example.pixel.node_execution.dto.NodeExecutionResponse;
import com.example.pixel.node_execution.dto.NodeValidationResponse;
import com.example.pixel.node.model.*;
import com.example.pixel.node_execution.cache.NodeCache;
import com.example.pixel.node_execution.integration.NodeClient;
import com.example.pixel.node_execution.executor.NodeExecutor;
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
class NodeExecutorTest {

    @Mock
    private NodeCache nodeCache;

    @Mock
    private Node node;

    @InjectMocks
    private NodeExecutor nodeExecutor;

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
    void executeInternal_shouldCacheInputsAndOutputs() {
        when(nodeClient.validateNode(any(NodeClientData.class))).thenReturn(nodeValidationResponse);
        when(nodeClient.executeNode(any(NodeClientData.class))).thenReturn(nodeExecutionResponse);

        nodeExecutor.execute(node,  sceneId, taskId);

        verify(nodeCache).put("100:10:input", inputs);
        verify(nodeCache).put("100:10:output", outputs);
    }

    @Test
    void resolveReference_shouldThrowWhenOutputDoesNotExist() {
        NodeReference reference = new NodeReference("@node:20:nonexistent");
        inputs.put("refParam", reference);

        Map<String, Object> referencedOutput = new HashMap<>();

        when(nodeCache.exists(taskId + ":" + nodeId + ":output")).thenReturn(true);
        when(nodeCache.get(taskId + ":" + nodeId + ":output")).thenReturn(referencedOutput);

        assertThrows(RuntimeException.class, () ->
                nodeExecutor.execute(node, sceneId, taskId));
    }

    @Test
    void resolveReference_shouldThrowWhenCacheKeyDoesNotExist() {
        NodeReference reference = new NodeReference("@node:20:result");
        inputs.put("refParam", reference);

        when(nodeCache.exists(taskId + ":" + nodeId + ":output")).thenReturn(false);

        assertThrows(NodeExecutionException.class, () ->
                nodeExecutor.execute(node, sceneId, taskId));
    }

    @Test
    void executeInternal_shouldHandleValidationFailure() {
        when(nodeClient.validateNode(any(NodeClientData.class))).thenThrow(NodeExecutionException.class);
        when(nodeClient.executeNode(any(NodeClientData.class))).thenReturn(nodeExecutionResponse);

        assertThrows(NodeExecutionException.class, () ->
                nodeExecutor.execute(node, sceneId, taskId));
    }

    @Test
    void executeInternal_shouldHandleExecutionFailure() {
        when(nodeClient.validateNode(any(NodeClientData.class))).thenReturn(nodeValidationResponse);
        when(nodeClient.executeNode(any(NodeClientData.class))).thenThrow(NodeExecutionException.class);

        assertThrows(NodeExecutionException.class, () ->
                nodeExecutor.execute(node, sceneId, taskId));

        verify(nodeCache).put(taskId + ":" + nodeId + ":input", inputs);
        verify(nodeCache, never()).put(eq(taskId + ":" + nodeId + ":output"), any());
    }

    @Test
    void resolveInputs_shouldHandleEmptyInputs() {
        inputs.clear();

        when(nodeClient.executeNode(any(NodeClientData.class))).thenReturn(nodeExecutionResponse);

        nodeExecutor.execute(node, sceneId, taskId);

        verify(node).setInputs(inputsCaptor.capture());
        Map<String, Object> resolvedInputs = inputsCaptor.getValue();

        assertTrue(resolvedInputs.isEmpty());
    }
}