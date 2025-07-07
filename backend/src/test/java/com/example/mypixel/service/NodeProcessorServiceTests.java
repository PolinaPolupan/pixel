package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.NodeReference;
import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.Vector2D;
import com.example.mypixel.model.node.Node;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private Node node;

    @InjectMocks
    private NodeProcessorService nodeProcessorService;

    private final Long sceneId = 1L;
    private final Long taskId = 100L;
    private Map<String, Object> inputs = new HashMap<>();
    private Map<String, Object> outputs = new HashMap<>();

    @Test
    void processNode_shouldTrackOperationWithCorrectTags() {
        inputs = new HashMap<>();
        outputs = new HashMap<>();

        when(node.getId()).thenReturn(1L);
        when(node.getType()).thenReturn("testNode");
        when(node.getInputs()).thenReturn(inputs);
        when(node.exec()).thenReturn(outputs);

        doAnswer(inv -> {
            Runnable runnable = inv.getArgument(2);
            runnable.run();
            return null;
        }).when(performanceTracker).trackOperation(anyString(), any(Tags.class), any(Runnable.class));

        nodeProcessorService.processNode(node, sceneId, taskId);

        verify(performanceTracker).trackOperation(
                eq("node.execution"),
                eq(Tags.of(
                        "node.id", "1",
                        "node.type", "testNode",
                        "scene.id", "1",
                        "task.id", "100"
                )),
                any(Runnable.class)
        );
    }

    @Test
    void processNodeInternal_shouldAutowireAndSetupNode() {
        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(beanFactory).autowireBean(node);
        verify(node).setFileHelper(any(FileHelper.class));
        verify(node).setBatchProcessor(batchProcessor);
    }

    @Test
    void processNodeInternal_shouldExecuteNodeAndCacheResults() {
        when(node.getId()).thenReturn(1L);

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        verify(node).validate();
        verify(node).exec();
        verify(nodeCacheService).put("100:1:input", inputs);
        verify(nodeCacheService).put("100:1:output", outputs);
    }

    @Test
    void resolveInputs_shouldResolveAllInputs() {
        Map<String, Object> originalInputs = new HashMap<>();
        originalInputs.put("textInput", "Hello");
        originalInputs.put("numInput", 42);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("textInput", Parameter.required(ParameterType.STRING));
        inputTypes.put("numInput", Parameter.required(ParameterType.INT));

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        ArgumentCaptor<Map<String, Object>> resolvedInputCaptor = ArgumentCaptor.forClass(Map.class);
        verify(node).setInputs(resolvedInputCaptor.capture());

        Map<String, Object> resolvedInputs = resolvedInputCaptor.getValue();
        assertEquals("Hello", resolvedInputs.get("textInput"));
        assertEquals(42, resolvedInputs.get("numInput"));
    }

    @Test
    void resolveNodeReference_shouldFetchFromCache() {
        Map<String, Object> originalInputs = new HashMap<>();
        NodeReference reference = new NodeReference("@node:2:color");
        originalInputs.put("colorInput", reference);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("colorInput", Parameter.required(ParameterType.STRING));

        Map<String, Object> cachedOutput = new HashMap<>();
        cachedOutput.put("color", "#FF0000");

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);
        when(nodeCacheService.exists("100:2:output")).thenReturn(true);
        when(nodeCacheService.get("100:2:output")).thenReturn(cachedOutput);

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        ArgumentCaptor<Map<String, Object>> resolvedInputCaptor = ArgumentCaptor.forClass(Map.class);
        verify(node).setInputs(resolvedInputCaptor.capture());

        Map<String, Object> resolvedInputs = resolvedInputCaptor.getValue();
        assertEquals("#FF0000", resolvedInputs.get("colorInput"));
    }

    @Test
    void castTypes_shouldConvertToInt() {
        Map<String, Object> originalInputs = new HashMap<>();
        originalInputs.put("intInput", 42.5);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("intInput", Parameter.required(ParameterType.INT));

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        ArgumentCaptor<Map<String, Object>> resolvedInputCaptor = ArgumentCaptor.forClass(Map.class);
        verify(node).setInputs(resolvedInputCaptor.capture());

        Map<String, Object> resolvedInputs = resolvedInputCaptor.getValue();
        assertEquals(42, resolvedInputs.get("intInput"));
        assertInstanceOf(Integer.class, resolvedInputs.get("intInput"));
    }

    @Test
    void castTypes_shouldConvertToFloat() {
        Map<String, Object> originalInputs = new HashMap<>();
        originalInputs.put("floatInput", 42);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("floatInput", Parameter.required(ParameterType.FLOAT));

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        ArgumentCaptor<Map<String, Object>> resolvedInputCaptor = ArgumentCaptor.forClass(Map.class);
        verify(node).setInputs(resolvedInputCaptor.capture());

        Map<String, Object> resolvedInputs = resolvedInputCaptor.getValue();
        assertEquals(42.0f, resolvedInputs.get("floatInput"));
        assertInstanceOf(Float.class, resolvedInputs.get("floatInput"));
    }

    @Test
    void castTypes_shouldConvertToVector2D_fromMap() {
        Map<String, Object> vectorMap = new HashMap<>();
        vectorMap.put("x", 10.0);
        vectorMap.put("y", 20.0);

        Map<String, Object> originalInputs = new HashMap<>();
        originalInputs.put("vectorInput", vectorMap);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("vectorInput", Parameter.required(ParameterType.VECTOR2D));

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        ArgumentCaptor<Map<String, Object>> resolvedInputCaptor = ArgumentCaptor.forClass(Map.class);
        verify(node).setInputs(resolvedInputCaptor.capture());

        Map<String, Object> resolvedInputs = resolvedInputCaptor.getValue();
        assertInstanceOf(Vector2D.class, resolvedInputs.get("vectorInput"));
        Vector2D vector = (Vector2D) resolvedInputs.get("vectorInput");
        assertEquals(10.0, vector.getX());
        assertEquals(20.0, vector.getY());
    }

    @Test
    void processNodeInternal_shouldThrowWhenNodeValidationFails() {
        doThrow(new RuntimeException("Validation failed")).when(node).validate();

        assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));
    }

    @Test
    void resolveInput_shouldThrowOnTypeMismatch() {
        Map<String, Object> originalInputs = new HashMap<>();
        originalInputs.put("stringInput", 42);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("stringInput", Parameter.required(ParameterType.STRING));

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);
        when(node.getId()).thenReturn(1L);

        InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Invalid input parameter 'stringInput'"));
    }

    @Test
    void resolveReference_shouldThrowOnMissingReferenceOutput() {
        Map<String, Object> originalInputs = new HashMap<>();
        NodeReference reference = new NodeReference("@node:2:nonExistentOutput");
        originalInputs.put("refInput", reference);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("refInput", Parameter.required(ParameterType.STRING));

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Failed to resolve reference"));
    }

    @Test
    void castTypes_shouldProcessFilepathArrayWithBatchProcessor() {
        List<String> filepaths = Arrays.asList("file1.jpg", "file2.jpg", "file3.jpg");

        Map<String, Object> originalInputs = new HashMap<>();
        originalInputs.put("files", filepaths);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("files", Parameter.required(ParameterType.FILEPATH_ARRAY));

        FileHelper mockFileHelper = mock(FileHelper.class);
        when(mockFileHelper.createDump(anyString())).thenAnswer(inv ->
                "processed_" + inv.getArgument(0));

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);
        when(node.getFileHelper()).thenReturn(mockFileHelper);

        doAnswer(inv -> {
            Collection<?> items = inv.getArgument(0);
            Consumer<?> consumer = inv.getArgument(1);
            for (Object item : items) {
                ((Consumer<Object>) consumer).accept(item);
            }
            return null;
        }).when(batchProcessor).processBatches(any(Collection.class), any(Consumer.class));

        nodeProcessorService.processNodeInternal(node, sceneId, taskId);

        ArgumentCaptor<Map<String, Object>> resolvedInputCaptor = ArgumentCaptor.forClass(Map.class);
        verify(node).setInputs(resolvedInputCaptor.capture());

        Map<String, Object> resolvedInputs = resolvedInputCaptor.getValue();
        Set<String> processedFiles = (Set<String>) resolvedInputs.get("files");

        assertEquals(3, processedFiles.size());
        assertTrue(processedFiles.contains("processed_file1.jpg"));
        assertTrue(processedFiles.contains("processed_file2.jpg"));
        assertTrue(processedFiles.contains("processed_file3.jpg"));
    }

    @Test
    void castTypes_shouldThrowOnInvalidFilepathArray() {
        List<Object> invalidFilepaths = Arrays.asList("file1.jpg", 123, "file3.jpg");

        Map<String, Object> originalInputs = new HashMap<>();
        originalInputs.put("files", invalidFilepaths);

        Map<String, Parameter> inputTypes = new HashMap<>();
        inputTypes.put("files", Parameter.required(ParameterType.FILEPATH_ARRAY));

        FileHelper mockFileHelper = mock(FileHelper.class);
        when(mockFileHelper.createDump(anyString())).thenReturn("processed_file");

        when(node.getInputs()).thenReturn(originalInputs);
        when(node.getInputTypes()).thenReturn(inputTypes);
        when(node.getFileHelper()).thenReturn(mockFileHelper);

        doAnswer(inv -> {
            Collection<?> items = inv.getArgument(0);
            Consumer<?> consumer = inv.getArgument(1);
            for (Object item : items) {
                ((Consumer<Object>) consumer).accept(item);
            }
            return null;
        }).when(batchProcessor).processBatches(any(Collection.class), any(Consumer.class));

        InvalidNodeParameter exception = assertThrows(InvalidNodeParameter.class, () ->
                nodeProcessorService.processNodeInternal(node, sceneId, taskId));

        assertTrue(exception.getMessage().contains("Invalid file path"));
    }
}
