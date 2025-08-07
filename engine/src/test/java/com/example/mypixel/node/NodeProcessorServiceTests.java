package com.example.mypixel.node;

import com.example.mypixel.common.PerformanceTracker;
import io.micrometer.core.instrument.Tags;
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
class NodeProcessorServiceTests {

    @Mock
    private NodeCacheService nodeCacheService;

    @Mock
    private PerformanceTracker performanceTracker;

    @Mock
    private Node node;

    @InjectMocks
    private NodeProcessorService nodeProcessorService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> inputsCaptor;

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
    }

    @Test
    void processNode_shouldTrackOperation() {
        doAnswer(inv -> {
            Runnable runnable = inv.getArgument(2);
            runnable.run();
            return null;
        }).when(performanceTracker).trackOperation(anyString(), any(Tags.class), any(Runnable.class));

        nodeProcessorService.processNode(node, sceneId, taskId);

        verify(performanceTracker).trackOperation(
                eq("node.execution"),
                eq(Tags.of(
                        "node.id", nodeId.toString(),
                        "node.type", "testNode",
                        "scene.id", sceneId.toString(),
                        "task.id", taskId.toString()
                )),
                any(Runnable.class)
        );
    }

    @Test
    void processNodeInternal_shouldResolveInputsValidateAndExecute() {
        nodeProcessorService.processNodeInternal(node, sceneId, taskId);
    }

    @Test
    void processNodeInternal_shouldCacheInputsAndOutputs() {
        nodeProcessorService.processNodeInternal(node,  sceneId, taskId);

        verify(nodeCacheService).put("100:10:input", inputs);
        verify(nodeCacheService).put("100:10:output", outputs);
    }

    @Test
    void resolveReference_shouldThrowWhenOutputDoesNotExist() {
        NodeReference reference = new NodeReference("@node:20:nonexistent");
        inputs.put("refParam", reference);

        Map<String, Object> referencedOutput = new HashMap<>();

        when(nodeCacheService.exists("100:20:output")).thenReturn(true);
        when(nodeCacheService.get("100:20:output")).thenReturn(referencedOutput);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Failed to resolve reference"));
    }

    @Test
    void resolveReference_shouldThrowWhenCacheKeyDoesNotExist() {
        NodeReference reference = new NodeReference("@node:20:result");
        inputs.put("refParam", reference);

        when(nodeCacheService.exists("100:20:output")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Failed to resolve reference"));
    }

    @Test
    void resolveInput_shouldThrowInvalidNodeParameterOnConversionError() {
        inputs.put("param1", "not a number");

        when(node.getId()).thenReturn(10L);

        ClassCastException exception = assertThrows(ClassCastException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Cannot cast String to Integer"));
    }

    @Test
    void processNodeInternal_shouldHandleValidationFailure() {

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertEquals("Validation failed", exception.getMessage());
    }

    @Test
    void processNodeInternal_shouldHandleExecutionFailure() {

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertEquals("Execution failed", exception.getMessage());
        verify(nodeCacheService).put("100:10:input", inputs);
        verify(nodeCacheService, never()).put(eq("100:10:output"), any());
    }

    @Test
    void resolveInputs_shouldHandleEmptyInputs() {
        inputs.clear();

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(node).setInputs(inputsCaptor.capture());
        Map<String, Object> resolvedInputs = inputsCaptor.getValue();

        assertTrue(resolvedInputs.isEmpty());
    }

    @Test
    void processNode_shouldHandlePerformanceTrackerFailure() {
        doThrow(new RuntimeException("Tracker failed")).when(performanceTracker)
                .trackOperation(anyString(), any(Tags.class), any(Runnable.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNode(node, sceneId, taskId));

        assertEquals("Tracker failed", exception.getMessage());
    }
}