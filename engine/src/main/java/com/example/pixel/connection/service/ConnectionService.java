package com.example.pixel.connection.service;

import com.example.pixel.common.exception.ConnectionNotFoundException;
import com.example.pixel.connection.dto.ConnectionPayload;
import com.example.pixel.connection.dto.ConnectionRequest;
import com.example.pixel.connection.entity.ConnectionEntity;
import com.example.pixel.connection.mapper.ConnectionMapper;
import com.example.pixel.connection.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository repository;
    private final ConnectionMapper connectionMapper;

    @Transactional
    public ConnectionPayload create(ConnectionRequest request) {
        ConnectionEntity connectionEntity = repository.save(connectionMapper.toEntity(request));
        return connectionMapper.toDto(connectionEntity);
    }

    @Transactional(readOnly = true)
    public ConnectionPayload findByConnId(String connId) {
        ConnectionEntity conn = repository
                .findByConnId(connId)
                .orElseThrow(() -> new ConnectionNotFoundException("Connection not found: " + connId));

        return connectionMapper.toDto(conn);
    }

    public List<ConnectionPayload> findAll() {
        List<ConnectionEntity> connectionEntities = repository.findAll();
        return connectionEntities.stream()
                .map(connectionMapper::toDto)
                .toList();
    }

    @Transactional
    public void delete(String connId) {
        repository.deleteByConnId(connId);
    }
}
