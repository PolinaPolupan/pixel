package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@MyPixelNode("ResNet50")
public class ResNet50Node extends Node {

    private static final String PYTHON_SCRIPT_PATH = "/app/python/app.py";
    private static final String PYTHON_EXECUTABLE = "python3";

    @JsonCreator
    public ResNet50Node(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of("files", ParameterType.FILEPATH_ARRAY.required());
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("json", ParameterType.STRING);
    }

    @Override
    public Map<String, Object> exec() {
        HashMap<String, Object> outputs = new HashMap<>();
        HashSet<String> files = (HashSet<String>) inputs.get("files");

        File outputFile = fileHelper.createTempJson();

        batchProcessor.processBatchesList(files, file ->
            executeScript(file, outputFile, outputs)
        );

        return outputs;
    }

    private void executeScript(List<String> files,
                               File outputFile,
                               Map<String, Object> outputs) {
        try {
            String filesString = String.join(",", files);

            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_EXECUTABLE,
                    PYTHON_SCRIPT_PATH,
                    "--input", filesString,
                    "--output", outputFile.getAbsolutePath()
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output = captureProcessOutput(process);

            if (process.waitFor() != 0) {
                throw new RuntimeException("Python script failed: " + output);
            }

            if (!outputFile.exists()) {
                throw new RuntimeException("Output file was not created");
            }

            String jsonContent = Files.readString(outputFile.toPath());

            outputs.put("json", jsonContent);

        } catch (Exception e) {
            throw new RuntimeException("Error executing Python script ", e);
        }
    }

    private String captureProcessOutput(Process process) {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Python: {}", line);
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            log.warn("Error reading process output", e);
        }
        return output.toString();
    }

    @Override
    public void validate() {
    }
}