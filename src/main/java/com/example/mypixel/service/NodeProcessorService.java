package com.example.mypixel.service;

import com.example.mypixel.model.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class NodeProcessorService {

    private final StorageService tempStorageService;
    private final StorageService storageService;
    private final FilteringService filteringService;

    @Autowired
    public NodeProcessorService(
            @Qualifier("storageService") StorageService storageService,
            @Qualifier("tempStorageService") StorageService tempStorageService,
            FilteringService filteringService) {
        this.tempStorageService = tempStorageService;
        this.storageService = storageService;
        this.filteringService = filteringService;
    }

    public String processInputNode(Node node) {
        String filename = (String) node.getParams().get("filename");
        String tempFile = tempStorageService.createTempFileFromResource(storageService.loadAsResource(filename));
        log.info("InputNode processed");

        return tempFile;
    }

    public String processGaussianBlurNode(Node node, String inputFilename) {
        Map<String, Object> params = node.getParams();
        int sizeX = (int) params.getOrDefault("sizeX", 1);
        int sizeY = (int) params.getOrDefault("sizeY", 1);
        double sigmaX = (double) params.getOrDefault("sigmaX", 0.0);
        double sigmaY = (double) params.getOrDefault("sigmaY", 0.0);

        String tempFile = tempStorageService.createTempFileFromFilename(inputFilename);
        filteringService.gaussianBlur(tempFile, sizeX, sizeY, sigmaX, sigmaY);

        log.info("GaussianBlurNode processed");

        return tempFile;
    }

    public String processOutputNode(Node node, String inputFilename, String outputFilename) {
        Resource outputImage = tempStorageService.loadAsResource(inputFilename);

        String filename = outputFilename;
        if (node.getParams().get("prefix") != null) {
            filename = node.getParams().get("prefix") + "_" + outputFilename;
        }

        storageService.store(outputImage, filename);
        String tempFile = tempStorageService.createTempFileFromResource(outputImage);

        log.info("OutputNode processed");

        return tempFile;
    }
}
