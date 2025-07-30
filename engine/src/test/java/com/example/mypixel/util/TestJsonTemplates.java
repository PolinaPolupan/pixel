package com.example.mypixel.util;

import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class TestJsonTemplates {

    /**
     * Load a JSON template from resources and return as a string
     * @param templatePath Path to the template file in resources
     * @return The JSON template as a string
     */
    public static String loadJsonTemplate(String templatePath) {
        try (InputStream is = TestJsonTemplates.class.getClassLoader().getResourceAsStream(templatePath)) {
            if (is == null) {
                throw new RuntimeException("Template not found: " + templatePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template: " + templatePath, e);
        }
    }

    /**
     * Apply placeholders to a template
     * @param template The template string with placeholders
     * @param replacements Map of placeholder to value replacements
     * @return The processed template with placeholders replaced
     */
    public static String applyPlaceholders(String template, Map<String, String> replacements) {
        String result = template;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * Load a graph template and apply AWS test credentials
     * @param templatePath Path to the template in resources
     * @param sceneId The scene ID to use
     * @return Processed JSON string with test values
     */
    public static String getGraphJsonWithTestCredentials(String templatePath, Long sceneId,
                                                         LocalStackContainer localstack) {
        // Get the base template
        String template = loadJsonTemplate(templatePath);

        // Create replacements map
        Map<String, String> replacements = new HashMap<>();
        replacements.put("aws_access", TestcontainersExtension.getLocalstack().getAccessKey());
        replacements.put("aws_secret", TestcontainersExtension.getLocalstack().getSecretKey());
        replacements.put("aws_region", TestcontainersExtension.getLocalstack().getRegion());
        replacements.put("aws_bucket", TestcontainersExtension.getTestBucket());
        replacements.put("scene_id", String.valueOf(sceneId));
        replacements.put("aws_endpoint", localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());

        // Apply replacements
        return applyPlaceholders(template, replacements);
    }
}
