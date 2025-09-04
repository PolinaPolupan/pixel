package com.example.pixel.config;

import com.example.pixel.file_system.StorageService;
import com.example.pixel.file_system.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.directory}")
    private String location;

    @Bean
    public StorageService storageService() {
        StorageService service = new FileStorageService(location);
        service.deleteAll();
        service.init();
        return service;
    }
}
