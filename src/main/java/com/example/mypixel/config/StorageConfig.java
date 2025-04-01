package com.example.mypixel.config;

import com.example.mypixel.service.StorageService;
import com.example.mypixel.service.TempStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class StorageConfig {

    @Autowired
    private Environment env;

    @Bean
    public StorageService storageService() {
        String location = env.getProperty("storage.images");
        StorageService service = new TempStorageService(location);
        service.deleteAll();
        service.init();
        return service;
    }

    @Bean
    public StorageService tempStorageService() {
        String location = env.getProperty("storage.temp-images");
        StorageService service = new TempStorageService(location);
        service.deleteAll();
        service.init();
        return service;
    }
}
