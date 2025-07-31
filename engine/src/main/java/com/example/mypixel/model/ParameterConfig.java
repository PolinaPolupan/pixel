package com.example.mypixel.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ParameterConfig {
    private String type;
    private boolean required;
    private String widget;
}