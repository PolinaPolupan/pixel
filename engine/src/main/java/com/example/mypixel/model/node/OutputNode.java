package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.ParamsMap;
import com.example.mypixel.model.Widget;
import com.example.mypixel.service.FileHelper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;


@MyPixelNode("Output")
public class OutputNode extends Node {

    @JsonCreator
    public OutputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return ParamsMap.of(
                "files", Parameter.builder()
                        .type(ParameterType.FILEPATH_ARRAY)
                        .required(true)
                        .widget(Widget.LABEL)
                        .build(),
                "prefix", Parameter.builder()
                        .type(ParameterType.STRING)
                        .required(false)
                        .widget(Widget.INPUT)
                        .build(),
                "folder", Parameter.builder()
                        .type(ParameterType.STRING)
                        .required(false)
                        .widget(Widget.INPUT)
                        .build()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return ParamsMap.of(
                "files", new HashSet<>(),
                "prefix", "",
                "folder", ""
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
                "description", "Output files to a folder",
                "color", "#AED581",
                "icon", "OutputIcon"
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        String prefix = (String) inputs.getOrDefault("prefix", null);
        String folder = (String) inputs.getOrDefault("folder", null);

        Map<String, Object> outputs = Map.of();

        for (String filepath : files) {
            FileHelper.storeFromTaskToSceneContext(sceneId, filepath, folder, prefix);
        }

        return outputs;
    }

    @Override
    public void validate() {

    }
}
