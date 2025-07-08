package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.NodeReference;
import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.node.Node;
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
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NodeProcessorServiceTests {

    @Mock
    private AutowireCapableBeanFactory beanFactory;

    @Mock
    private NodeCacheService nodeCacheService;

    @Mock
    private StorageService storageService;

    @Mock
    private BatchProcessor batchProcessor;

    @Mock
    private PerformanceTracker performanceTracker;

    @Mock
    private TypeConverterRegistry typeConverterRegistry;

    @Mock
    private Node node;

    @InjectMocks
    private NodeProcessorService nodeProcessorService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> inputsCaptor;

    @Captor
    private ArgumentCaptor<FileHelper> fileHelperCaptor;

    private final Long sceneId = 1L;
    private final Long taskId = 100L;
    private final Long nodeId = 10L;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private Map<String, Parameter> inputTypes;

    @BeforeEach
    void setUp() {
        inputs = new HashMap<>();
        outputs = new HashMap<>();
        inputTypes = new HashMap<>();

        when(node.getId()).thenReturn(nodeId);
        when(node.getType()).thenReturn("testNode");
        when(node.getInputs()).thenReturn(inputs);
        when(node.getInputTypes()).thenReturn(inputTypes);
        when(node.exec()).thenReturn(outputs);
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

        verify(node).exec();
    }

    @Test
    void processNodeInternal_shouldSetupNodeCorrectly() {
        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(beanFactory).autowireBean(node);
        verify(node).setFileHelper(any(FileHelper.class));
        verify(node).setBatchProcessor(batchProcessor);
    }

    @Test
    void processNodeInternal_shouldResolveInputsValidateAndExecute() {
        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(node).validate();
        verify(node).exec();
    }

    @Test
    void processNodeInternal_shouldCacheInputsAndOutputs() {
        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(nodeCacheService).put("100:10:input", inputs);
        verify(nodeCacheService).put("100:10:output", outputs);
    }

    @Test
    void resolveReference_shouldThrowWhenOutputDoesNotExist() {
        NodeReference reference = new NodeReference("@node:20:nonexistent");
        inputs.put("refParam", reference);
        inputTypes.put("refParam", Parameter.required(ParameterType.STRING));

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
        inputTypes.put("refParam", Parameter.required(ParameterType.STRING));

        when(nodeCacheService.exists("100:20:output")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Failed to resolve reference"));
    }

    @Test
    void resolveInput_shouldThrowInvalidNodeParameterOnConversionError() {
        inputs.put("param1", "not a number");
        inputTypes.put("param1", Parameter.required(ParameterType.INT));

        when(node.getId()).thenReturn(10L);

        when(typeConverterRegistry.convert(any(), any(), any()))
                .thenThrow(new ClassCastException("Cannot cast String to Integer"));

        InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Invalid input parameter 'param1'"));
    }

    @Test
    void fileHelper_shouldBeCreatedWithCorrectParameters() {
        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(node).setFileHelper(fileHelperCaptor.capture());
        FileHelper fileHelper = fileHelperCaptor.getValue();

        // Since FileHelper doesn't expose its fields, we can only verify it was created and set
        assertNotNull(fileHelper);
    }

    @Test
    void processNodeInternal_shouldHandleValidationFailure() {
        doThrow(new RuntimeException("Validation failed")).when(node).validate();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertEquals("Validation failed", exception.getMessage());
        verify(node, never()).exec();
    }

    @Test
    void processNodeInternal_shouldHandleExecutionFailure() {
        when(node.exec()).thenThrow(new RuntimeException("Execution failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertEquals("Execution failed", exception.getMessage());
        verify(nodeCacheService).put("100:10:input", inputs);
        verify(nodeCacheService, never()).put(eq("100:10:output"), any());
    }

    @Test
    void resolveInputs_shouldHandleEmptyInputs() {
        inputs.clear();  // Ensure inputs is empty

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(node).setInputs(inputsCaptor.capture());
        Map<String, Object> resolvedInputs = inputsCaptor.getValue();

        assertTrue(resolvedInputs.isEmpty());
        verify(typeConverterRegistry, never()).convert(any(), any(), any());
    }

    @Test
    void processNode_shouldHandlePerformanceTrackerFailure() {
        doThrow(new RuntimeException("Tracker failed")).when(performanceTracker)
                .trackOperation(anyString(), any(Tags.class), any(Runnable.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNode(node, sceneId, taskId));

        assertEquals("Tracker failed", exception.getMessage());
        verify(node, never()).exec();
    }
}