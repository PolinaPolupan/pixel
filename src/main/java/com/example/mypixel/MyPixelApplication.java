package com.example.mypixel;

import com.example.mypixel.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class MyPixelApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyPixelApplication.class, args);
    }
}
