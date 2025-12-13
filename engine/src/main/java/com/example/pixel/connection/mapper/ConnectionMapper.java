package com.example.pixel.connection.mapper;

import com.example.pixel.common.exception.ConnectionCreationFailedException;
import com.example.pixel.connection.dto.ConnectionPayload;
import com.example.pixel.connection.dto.ConnectionRequest;
import com.example.pixel.connection.entity.ConnectionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Mapper(componentModel = "spring")
public abstract class ConnectionMapper {

    @Value("${conn.encrypt.key}")
    private String encryptKey;

    @Mapping(target = "login", source = "login", qualifiedByName = "decrypt")
    @Mapping(target = "password", source = "password", qualifiedByName = "decrypt")
    public abstract ConnectionPayload toDto(ConnectionEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "schema", ignore = true)
    @Mapping(target = "port", ignore = true)
    @Mapping(target = "login", source = "login", qualifiedByName = "encrypt")
    @Mapping(target = "password", source = "password", qualifiedByName = "encrypt")
    public abstract ConnectionEntity toEntity(ConnectionRequest request);

    private Cipher initCipher(int mode) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
                encryptKey.getBytes(StandardCharsets. UTF_8),
                "AES"
        );
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, keySpec);
        return cipher;
    }

    @SuppressWarnings("unused")
    @Named("encrypt")
    protected byte[] encrypt(String value) {
        if (value == null) return null;
        try {
            return initCipher(Cipher.ENCRYPT_MODE).doFinal(
                    value.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new ConnectionCreationFailedException("Failed to encrypt data", e);
        }
    }

    @SuppressWarnings("unused")
    @Named("decrypt")
    protected String decrypt(byte[] value) {
        if (value == null) return null;
        try {
            return new String(
                    initCipher(Cipher.DECRYPT_MODE).doFinal(value),
                    StandardCharsets. UTF_8
            );
        } catch (Exception e) {
            throw new ConnectionCreationFailedException("Failed to decrypt data", e);
        }
    }
}