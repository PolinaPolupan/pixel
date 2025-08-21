package com.example.pixel.file_system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatsPayload {
    private int totalFiles;
    private long totalSize;
    private int zipFiles;
    private int imageFiles;
    private List<String> locations;
}
