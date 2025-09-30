package com.example.pixel.node.mapper;

import com.example.pixel.node.dto.NodePayload;
import com.example.pixel.node.entity.NodeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NodeMapper {
    NodePayload toDto(NodeEntity node);
}
