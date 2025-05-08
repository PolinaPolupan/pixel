package com.example.mypixel.model.node;

import com.example.mypixel.model.InputDeserializer;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.service.BatchProcessor;
import com.example.mypixel.service.FileHelper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.Map;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
public abstract class Node {

    @NonNull
    @Setter(AccessLevel.NONE)
    Long id;

    @NonNull
    @Setter(AccessLevel.NONE)
    String type;

    @JsonDeserialize(contentUsing = InputDeserializer.class)
    Map<String, Object> inputs;

    FileHelper fileHelper;

    BatchProcessor batchProcessor;

    boolean cacheable = true;

    @JsonCreator
    public Node(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        this.id = id;
        this.type = type;
        this.inputs = inputs;
    }

    public abstract Map<String, ParameterType> getInputTypes();

    public abstract Map<String, Object> getDefaultInputs();

    public abstract Map<String, ParameterType> getOutputTypes();

    public abstract Map<String, String> getDisplayInfo();

    public abstract Map<String, Object> exec();

    public abstract void validate();
}
