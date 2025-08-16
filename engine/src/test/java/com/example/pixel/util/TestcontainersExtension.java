package com.example.pixel.util;

import lombok.Getter;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

public class TestcontainersExtension implements BeforeAllCallback {

    private static final Network SHARED_NETWORK = Network.newNetwork();

    @Getter
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withNetwork(SHARED_NETWORK)
            .withPassword("test");

    @Getter
    private static final GenericContainer<?> redis = new GenericContainer<>("redislabs/redismod")
            .withNetwork(SHARED_NETWORK)
            .withExposedPorts(6379);

    @Getter
    public static final LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:latest"))
            .withNetwork(SHARED_NETWORK)
            .withServices(S3);

    @Getter
    public static final GenericContainer<?> node = new GenericContainer<>(
            new ImageFromDockerfile()
                    .withDockerfile(Paths.get("../node/Dockerfile"))
                    .withBuildArg("BUILDKIT_INLINE_CACHE", "1")
    )
            .withExposedPorts(8000)
            .waitingFor(
                    Wait.forHttp("/health")
                            .forPort(8000)
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(2))
            );

    private static boolean started = false;

    private static final String TEST_BUCKET = "test-bucket";

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            postgres.start();
            redis.start();
            localstack.start();
            node.start();

            // Database configuration
            System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
            System.setProperty("spring.datasource.username", postgres.getUsername());
            System.setProperty("spring.datasource.password", postgres.getPassword());

            // Redis configuration
            System.setProperty("spring.data.redis.host", redis.getHost());
            System.setProperty("spring.data.redis.port", String.valueOf(redis.getMappedPort(6379)));

            // AWS configuration
            System.setProperty("aws.endpoint", localstack.getEndpointOverride(S3).toString());
            System.setProperty("aws.region", localstack.getRegion());
            System.setProperty("aws.access-key", localstack.getAccessKey());
            System.setProperty("aws.secret-key", localstack.getSecretKey());
            System.setProperty("aws.bucket", TEST_BUCKET);

            System.setProperty("node.service.url", "http://" + node.getHost() + ":" + node.getMappedPort(8000) + "/");

            // Initialize test bucket
            createTestBucket();

            started = true;
        }
    }

    private void createTestBucket() {
        try {
            S3Client s3Client = createS3Client();

            try {
                s3Client.headBucket(HeadBucketRequest.builder()
                        .bucket(TEST_BUCKET)
                        .build());
            } catch (Exception e) {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(TEST_BUCKET)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test bucket: " + TEST_BUCKET, e);
        }
    }

    public static S3Client createS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(localstack.getEndpointOverride(S3).toString()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                ))
                .region(Region.of(localstack.getRegion()))
                .forcePathStyle(true)
                .build();
    }

    public static String getTestBucket() {
        return TEST_BUCKET;
    }

    public static void uploadTestFileToS3(Long sceneId, String sourceFilePath, String s3KeyPath) {
        try {
            S3Client s3Client = createS3Client();

            Path path = Paths.get(Objects.requireNonNull(TestcontainersExtension.class.getClassLoader()
                    .getResource(sourceFilePath)).toURI());

            String s3Path = s3KeyPath.replace("{{scene_id}}", String.valueOf(sceneId));

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(TEST_BUCKET)
                            .key(s3Path)
                            .build(),
                    RequestBody.fromBytes(Files.readAllBytes(path))
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload test file to S3: " + e.getMessage(), e);
        }
    }

    public static boolean doesObjectExistInS3(String key) {
        try {
            S3Client s3Client = createS3Client();
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(TEST_BUCKET)
                    .key(key)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}