package com.example.mypixel.config;

import com.example.mypixel.model.InvalidNodeTypeHandler;
import com.example.mypixel.model.node.MyPixelNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class MyPixelConfig {

    @Bean
    public ObjectMapper graphObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        Reflections reflections = new Reflections("com.example.mypixel.model.node");
        Set<Class<?>> subtypes = reflections.getTypesAnnotatedWith(MyPixelNode.class);

        for (Class<?> subType : subtypes) {
            MyPixelNode annotation = subType.getAnnotation(MyPixelNode.class);
            if (annotation != null) {
                String typeName = annotation.value();
                objectMapper.registerSubtypes(new NamedType(subType, typeName));
            }
        }

        return objectMapper
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addHandler(new InvalidNodeTypeHandler());
    }
}
