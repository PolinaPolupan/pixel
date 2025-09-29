package com.example.pixel.graph_execution.mapper;

import com.example.pixel.graph_execution.dto.GraphExecutionPayload;
import com.example.pixel.graph_execution.entity.GraphExecutionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GraphExecutionMapper {
    GraphExecutionPayload toDto(GraphExecutionEntity graphExecutionEntity);
}
