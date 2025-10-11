package com.example.pixel.connection.dto;

import lombok.Data;

@Data
public class ConnectionRequest {
    private String connId;
    private String connType;
    private String host;
    private String schema;
    private String login;
    private String password;
    private Integer port;
    private String extra;
}
