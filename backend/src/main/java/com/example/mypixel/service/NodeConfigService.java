package com.example.mypixel.service;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.node.MyPixelNode;
import com.example.mypixel.model.node.Node;
import jakarta.annotation.PostConstruct;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.*;

@Service
public class NodeConfigService {
    private final Map<String, Object> completeNodesConfig = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        buildNodesConfig();
    }

    private void buildNodesConfig() {
        Reflections reflections = new Reflections("com.example.mypixel.model.node");
        Set<Class<?>> nodeClasses = reflections.getTypesAnnotatedWith(MyPixelNode.class);

        for (Class<?> clazz : nodeClasses) {
            if (Node.class.isAssignableFrom(clazz)) {
                try {
                    MyPixelNode annotation = clazz.getAnnotation(MyPixelNode.class);
                    String nodeType = annotation.value();

                    Constructor<?> constructor = clazz.getConstructor(Long.class, String.class, Map.class);
                    Node nodeInstance = (Node) constructor.newInstance(1L, nodeType, new LinkedHashMap<>());

                    Map<String, Object> nodeConfig = buildNodeConfig(nodeType, nodeInstance);
                    completeNodesConfig.put(nodeType, nodeConfig);

                } catch (Exception e) {
                    System.err.println("Error processing node class: " + clazz.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildNodeConfig(String nodeType, Node nodeInstance) {
        Map<String, Object> config = new LinkedHashMap<>();

        config.put("component", nodeType);

        Map<String, String> displayInfo = nodeInstance.getDisplayInfo();
        Map<String, Object> display = new LinkedHashMap<>();
        display.put("category", displayInfo.getOrDefault("category", "Uncategorized"));
        display.put("description", displayInfo.getOrDefault("description", ""));
        display.put("color", displayInfo.getOrDefault("color", "#78909C"));
        display.put("icon", displayInfo.getOrDefault("icon", "DefaultIcon"));
        config.put("display", display);

        config.put("defaultData", nodeInstance.getDefaultInputs());

        // Use LinkedHashMap to preserve order of handles
        Map<String, Object> handles = new LinkedHashMap<>();

        // Add input handles in their defined order
        Map<String, Parameter> inputTypes = nodeInstance.getInputTypes();
        for (Map.Entry<String, Parameter> entry : inputTypes.entrySet()) {
            String inputName = entry.getKey();
            ParameterType paramType = entry.getValue().getType();

            Map<String, String> handleInfo = new LinkedHashMap<>();
            handleInfo.put("target", paramType.toString());
            handleInfo.put("widget", entry.getValue().getWidget().toString());
            handles.put(inputName, handleInfo);
        }

        // Add output handles, updating any that are both input and output
        Map<String, Parameter> outputTypes = nodeInstance.getOutputTypes();
        for (Map.Entry<String, Parameter> entry : outputTypes.entrySet()) {
            String outputName = entry.getKey();
            ParameterType paramType = entry.getValue().getType();

            Map<String, String> handleInfo;
            if (handles.containsKey(outputName)) {
                // This handle is both input and output
                handleInfo = (Map<String, String>) handles.get(outputName);
            } else {
                handleInfo = new LinkedHashMap<>();
            }
            handleInfo.put("source", paramType.toString());
            handleInfo.put("widget", entry.getValue().getWidget().toString());
            handles.put(outputName, handleInfo);
        }

        config.put("handles", handles);

        return config;
    }

    public Map<String, Object> getNodesConfig() {
        return completeNodesConfig;
    }
}