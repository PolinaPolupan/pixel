package com.example.pixel.connection.service;

import com.example.pixel.connection.dto.ConnectionRequest;
import com.example.pixel.connection.entity.ConnectionEntity;
import com.example.pixel.connection.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository repository;

    public ConnectionEntity createConnection(ConnectionRequest request) {
        ConnectionEntity entity = ConnectionEntity.builder()
                .connId(request.getConnId())
                .connType(request.getConnType())
                .host(request.getHost())
                .schema(request.getSchema())
                .login(request.getLogin())
                .password(request.getPassword())
                .port(request.getPort() != null ? request.getPort() : 0)
                .extra(request.getExtra())
                .build();
        return repository.save(entity);
    }

    public ConnectionEntity getConnection(String id) {
        return repository.findByConnId(id)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + id));
    }
}
