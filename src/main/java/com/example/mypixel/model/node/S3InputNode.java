package com.example.mypixel.model.node;

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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@MyPixelNode("S3Input")
public class S3InputNode extends Node {

    @Autowired
    @Qualifier("tempStorageService")
    private StorageService tempStorageService;

    @JsonCreator
    public S3InputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
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
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILENAMES_ARRAY);
    }

    @Override
    public Map<String, Object> exec() {
        Map<String, Object> outputs;

        String accessKey = (String) inputs.get("access_key_id");
        String secretKey = (String) inputs.get("secret_access_key");
        String regionName = (String) inputs.get("region");
        String bucket = (String) inputs.get("bucket");

        List<String> files = new ArrayList<>();

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

            for (S3Object file: contents) {
                String filename = file.key();
                InputStream in = s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(filename).build());

                tempStorageService.store(in, filename);
                files.add(filename);
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        outputs = Map.of("files", files);
        return outputs;
    }
}
