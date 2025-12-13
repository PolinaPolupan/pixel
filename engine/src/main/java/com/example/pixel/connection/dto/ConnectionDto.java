package com.example.pixel.connection.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionDto {
    private String connId;
    private String connType;
    private String host;
    private String login;
    private String password;
    private String extra;
}
