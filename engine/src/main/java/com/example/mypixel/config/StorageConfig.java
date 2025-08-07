package com.example.mypixel.config;

import com.example.mypixel.file_system.StorageService;
import com.example.mypixel.file_system.TempStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.directory}")
    private String location;

    @Bean
    public StorageService storageService() {
        StorageService service = new TempStorageService(location);
        service.deleteAll();
        service.init();
        return service;
    }
}
