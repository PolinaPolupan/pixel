package com.example.pixel.node_execution.mapper;

import com.example.pixel.node_execution.dto.NodeExecutionDto;
import com.example.pixel.node_execution.entity.NodeExecutionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NodeExecutionMapper {
    NodeExecutionDto toDto(NodeExecutionEntity node);
}
