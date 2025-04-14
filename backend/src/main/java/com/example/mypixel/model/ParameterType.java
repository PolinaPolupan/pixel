package com.example.mypixel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ParameterType {
    FLOAT,
    INT,
    DOUBLE,
    STRING,
    STRING_ARRAY,
    FILEPATH_ARRAY;

    private boolean required = false;

    public ParameterType required() {
        return required(true);
    }

    public ParameterType optional() {
        return required(false);
    }

    private ParameterType required(boolean required) {
        ParameterType copy = this;
        try {
            Field field = ParameterType.class.getDeclaredField("required");
            field.setAccessible(true);
            field.set(copy, required);
        } catch (Exception e) {
            throw new RuntimeException("Could not set required flag", e);
        }
        return copy;
    }
}
