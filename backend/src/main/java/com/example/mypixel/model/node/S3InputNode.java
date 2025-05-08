package com.example.mypixel.model.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@MyPixelNode("S3Input")
@Slf4j
public class S3InputNode extends Node {

    @JsonCreator
    public S3InputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public String getCategory() {
        return "IO";
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "access_key_id", ParameterType.STRING.required(),
                "secret_access_key", ParameterType.STRING.required(),
                "region", ParameterType.STRING.required(),
                "bucket", ParameterType.STRING.required()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "access_key_id", "",
                "secret_access_key", "",
                "region", "",
                "bucket", ""
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILEPATH_ARRAY);
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "description", "Load files from S3",
                "color", "#AED581",
                "icon", "S3Icon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        Map<String, Object> outputs;

        String accessKey = (String) inputs.get("access_key_id");
        String secretKey = (String) inputs.get("secret_access_key");
        String regionName = (String) inputs.get("region");
        String bucket = (String) inputs.get("bucket");

        HashSet<String> files = new HashSet<>();

        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        try (S3Client s3Client = S3Client
                .builder()
                .region(Region.of(regionName))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()) {

            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .build();
            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

            List<S3Object> contents = listObjectsV2Response.contents();

            batchProcessor.processBatches(
                    contents, file -> {
                        String filename = file.key();
                        log.debug("Loading file from S3: {}", filename);
                        InputStream in = s3Client
                                .getObject(GetObjectRequest.builder().bucket(bucket).key(filename).build());

                        files.add(fileHelper.storeToTemp(in, filename));
                        try {
                            in.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        outputs = Map.of("files", files);
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
