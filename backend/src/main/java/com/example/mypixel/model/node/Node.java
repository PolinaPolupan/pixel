package com.example.mypixel.model.node;

import com.example.mypixel.model.InputDeserializer;
import com.example.mypixel.model.NodeConfig;
import com.example.mypixel.model.Parameter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.File;
import java.util.Map;

@Slf4j
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
public abstract class Node {

    @NonNull
    @Setter(AccessLevel.NONE)
    Long id;

    Long sceneId;

    Long taskId;

    @NonNull
    @Setter(AccessLevel.NONE)
    String type;

    @JsonDeserialize(contentUsing = InputDeserializer.class)
    Map<String, Object> inputs;

    @JsonCreator
    public Node(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        this.id = id;
        this.type = type;
        this.inputs = inputs;
    }

    public abstract Map<String, Parameter> getInputTypes();

    public abstract Map<String, Object> getDefaultInputs();

    public abstract Map<String, Parameter> getOutputTypes();

    public abstract Map<String, String> getDisplayInfo();

    public abstract Map<String, Object> exec();

    public abstract void validate();

    /**
     * Dynamically finds and loads the config file based on the class name.
     * The filename must match the class name with .json extension.
     * @param configDir directory where configs are stored (e.g. "/app/node-configs")
     * @return NodeConfig object or null if not found or failed to parse
     */
    public NodeConfig findConfig(String configDir) {
        String configFilename = configDir + ".json";
        File configFile = new File(configFilename);

        if (!configFile.exists()) {
            log.warn("Config file not found for {}: {}", getClass().getSimpleName(), configFile.getAbsolutePath());
            return null;
        } else {
            log.info("Config file found for {}: {}", getClass().getSimpleName(), configFile.getAbsolutePath());
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            NodeConfig config = mapper.readValue(configFile, NodeConfig.class);
            log.info("Successfully loaded config for {} from {}", getClass().getSimpleName(), configFile.getAbsolutePath());
            return config;
        } catch (Exception e) {
            log.error("Failed to load config for {} from {}", getClass().getSimpleName(), configFile.getAbsolutePath(), e);
            return null;
        }
    }
}