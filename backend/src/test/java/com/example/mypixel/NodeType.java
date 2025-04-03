package com.example.mypixel;

public enum NodeType {
    INPUT("Input"),
    GAUSSIAN_BLUR("GaussianBlur"),
    OUTPUT("Output"),
    FLOOR("Floor"),
    S3OUTPUT("S3Output"),
    S3INPUT("S3Input"),
    COMBINE("Combine"),
    UNKNOWN("Unknown");

    private final String name;

    NodeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}