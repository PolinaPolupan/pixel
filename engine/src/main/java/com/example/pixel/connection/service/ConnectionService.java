package com.example.pixel.connection.service;

import com.example.pixel.connection.dto.ConnectionPayload;
import com.example.pixel.connection.dto.ConnectionRequest;
import com.example.pixel.connection.entity.ConnectionEntity;
import com.example.pixel.connection.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository repository;

    @Value("${conn.encrypt.key}")
    private String encryptKey;

    private Cipher initCipher(int mode) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(encryptKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, keySpec);
        return cipher;
    }

    private byte[] encrypt(String value) throws Exception {
        if (value == null) return null;
        return initCipher(Cipher.ENCRYPT_MODE).doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decrypt(byte[] value) throws Exception {
        if (value == null) return null;
        return new String(initCipher(Cipher.DECRYPT_MODE).doFinal(value), StandardCharsets.UTF_8);
    }

    @Transactional
    public ConnectionEntity create(ConnectionRequest request) {
        try {
            ConnectionEntity conn = ConnectionEntity.builder()
                    .connId(request.getConnId())
                    .connType(request.getConnType())
                    .host(request.getHost())
                    .login(encrypt(request.getLogin()))
                    .password(encrypt(request.getPassword()))
                    .extra(request.getExtra())
                    .build();
            return repository.save(conn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt connection data", e);
        }
    }

    @Transactional(readOnly = true)
    public ConnectionPayload get(String connId) {
        ConnectionEntity conn = repository
                .findByConnId(connId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        try {
            return ConnectionPayload.builder()
                    .connId(conn.getConnId())
                    .connType(conn.getConnType())
                    .host(conn.getHost())
                    .login(decrypt(conn.getLogin()))
                    .password(decrypt(conn.getPassword()))
                    .extra(conn.getExtra())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt connection data", e);
        }
    }

    @Transactional
    public void delete(String connId) {
        repository.deleteByConnId(connId);
    }
}
