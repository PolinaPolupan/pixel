package com.example.mypixel.model;

import lombok.Getter;

@Getter
public class Parameter {
    private final ParameterType type;
    private final boolean required;

    private Parameter(ParameterType type, boolean required) {
        this.type = type;
        this.required = required;
    }

    public static Parameter required(ParameterType type) {
        return new Parameter(type, true);
    }

    public static Parameter optional(ParameterType type) {
        return new Parameter(type, false);
    }
}