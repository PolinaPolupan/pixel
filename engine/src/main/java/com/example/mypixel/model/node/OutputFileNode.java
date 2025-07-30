package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.ParamsMap;
import com.example.mypixel.model.Widget;
import com.example.mypixel.service.FileHelper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.Map;

@MyPixelNode("OutputFile")
public class OutputFileNode extends Node {

    @JsonCreator
    public OutputFileNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return ParamsMap.of(
                "content", Parameter.builder()
                        .type(ParameterType.STRING)
                        .required(false)
                        .widget(Widget.INPUT)
                        .build(),
                "filename", Parameter.builder()
                        .type(ParameterType.STRING)
                        .required(false)
                        .widget(Widget.INPUT)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "content", "",
                "filename", "new.txt"
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return ParamsMap.of();
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return ParamsMap.of(
                "category", "IO",
                "description", "Output to a file",
                "color", "#AED581",
                "icon", "OutputIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        String content = (String) inputs.getOrDefault("content", "");
        String filename = (String) inputs.getOrDefault("filename", "new.txt");

        FileHelper.storeFile(sceneId, filename, content);

        return Map.of();
    }

    @Override
    public void validate() {

    }
}
