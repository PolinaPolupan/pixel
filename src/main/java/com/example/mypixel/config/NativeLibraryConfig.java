package com.example.mypixel.config;

import com.example.mypixel.service.FilteringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NativeLibraryConfig {

    static {
        try {
            System.loadLibrary("native-processor");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load: " + e.getMessage());
            throw e;
        }
    }

    @Bean
    public FilteringService filteringService() {
        return new FilteringService();
    }
}