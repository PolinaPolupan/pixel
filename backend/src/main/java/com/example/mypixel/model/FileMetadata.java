package com.example.mypixel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String name;

    private String relativeStoragePath;

    private String storagePath;

    private String checkSum;

    @PrePersist
    public void prePersist() {
        if (checkSum == null && storagePath != null) {
            try {
                calculateChecksum(storagePath);
            } catch (Exception e) {
                System.err.println("Failed to calculate checksum: " + e.getMessage());
            }
        }
    }

    public void calculateChecksum(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(
                    new FileInputStream(filePath), md)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {}

                md = dis.getMessageDigest();
            }

            this.checkSum = convertToHex(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    public void calculateChecksum(String filePath, int bufferSizeMB) {
        if (bufferSizeMB <= 0) bufferSizeMB = 16;

        int bufferSize = bufferSizeMB * 1048576;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            try (FileInputStream fis = new FileInputStream(filePath);
                 DigestInputStream dis = new DigestInputStream(fis, md)) {

                byte[] buffer = new byte[bufferSize];
                int bytesRead;

                while ((bytesRead = dis.read(buffer, 0, bufferSize)) != -1) {}

                md = dis.getMessageDigest();
            }

            this.checkSum = convertToHex(md.digest());

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Failed to calculate checksum: " + e.getMessage(), e);
        }
    }

    private String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }
}