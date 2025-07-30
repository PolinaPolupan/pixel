package com.example.mypixel.model.node;

import com.example.mypixel.model.InputDeserializer;
import com.example.mypixel.model.Parameter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Map;

@Slf4j
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
public abstract class Node {

    @NonNull
    @Setter(AccessLevel.NONE)
    protected Long id;

    protected Long sceneId;

    protected Long taskId;

    @NonNull
    @Setter(AccessLevel.NONE)
    protected String type;

    @JsonDeserialize(contentUsing = InputDeserializer.class)
    protected Map<String, Object> inputs;

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
}