package com.example.pixel.node;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NodeValidationResponse {
    private String status;
    private String error;
}