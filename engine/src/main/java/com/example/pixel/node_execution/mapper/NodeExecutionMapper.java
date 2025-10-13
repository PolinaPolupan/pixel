package com.example.pixel.node_execution.mapper;

import com.example.pixel.node_execution.dto.NodeExecutionPayload;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NodeExecutionMapper {
    NodeExecutionPayload toDto(NodeExecutionEntity node);
}
