package com.example.mypixel.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Parameter {
    private final ParameterType type;
    private final boolean required;
    private final Widget widget;

    private Parameter(ParameterType type, boolean required, Widget widget) {
        this.type = type;
        this.required = required;
        this.widget = widget;
    }

    public static Parameter required(ParameterType type) {
        return new Parameter(type, true, Widget.LABEL);
    }
}