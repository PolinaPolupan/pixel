package com.example.mypixel.service;

import com.example.mypixel.config.TestCacheConfig;
import com.example.mypixel.util.TestFileUtils;
import com.example.mypixel.util.TestGraphFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import({TestCacheConfig.class})
@ActiveProfiles("test")
public class GraphServiceTests {

    @Autowired
    private GraphService graphService;

    @Autowired
    private StorageService storageService;

    private final Long sceneId = 1L;

    @BeforeEach
    void setupTestFiles() {
        TestFileUtils.copyResourcesToDirectory(
                "upload-image-dir/scenes/" + sceneId + "/input/",
                "test-images/Picture1.png",
                "test-images/Picture3.png"
        );
    }

    @Test
    void executeGraph_defaultCase_shouldGenerateOutputFiles() {
        graphService.startGraphExecution(TestGraphFactory.getDefaultGraph(sceneId), sceneId);

        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/processed/filtered_result_Picture1.png").exists());
        assertTrue(storageService.loadAsResource("scenes/" +
                sceneId + "/output/processed/filtered_result_Picture3.png").exists());
    }
}
