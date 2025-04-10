package com.example.mypixel.model.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.service.StorageService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MyPixelNode("S3Output")
public class S3OutputNode extends Node {

    @Autowired
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @JsonCreator
    public S3OutputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files", ParameterType.FILENAMES_ARRAY.required(),
                "access_key_id", ParameterType.STRING.required(),
                "secret_access_key", ParameterType.STRING.required(),
                "region", ParameterType.STRING.required(),
                "bucket", ParameterType.STRING.required()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of();
    }

    @Override
    public Map<String, Object> exec() {
        List<String> files = (List<String>) inputs.get("files");
        Map<String, Object> outputs = Map.of();

        String accessKey = (String) inputs.get("access_key_id");
        String secretKey = (String) inputs.get("secret_access_key");
        String regionName = (String) inputs.get("region");
        String bucket = (String) inputs.get("bucket");

        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        try (S3Client s3Client = S3Client
                .builder()
                .region(Region.of(regionName))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()) {

            for (String file : files) {
                String filename = tempStorageService.removeExistingPrefix(file);
                Map<String, String> metadata = new HashMap<>();

                s3Client.putObject(request ->
                                request
                                        .bucket(bucket)
                                        .key(filename)
                                        .metadata(metadata),
                        tempStorageService.load(file));
            }
        }

        return outputs;
    }

    @Override
    public void validate() {
        if (inputs.get("access_key_id").toString().isEmpty()) {
            throw new InvalidNodeParameter("Access key ID cannot be blank.");
        }
        if (inputs.get("secret_access_key").toString().isEmpty()) {
            throw new InvalidNodeParameter("Secret cannot be blank.");
        }
        if (inputs.get("region").toString().isEmpty()) {
            throw new InvalidNodeParameter("Region cannot be blank.");
        }
        if (inputs.get("bucket").toString().isEmpty()) {
            throw new InvalidNodeParameter("Bucket cannot be blank.");
        }
    }
}
