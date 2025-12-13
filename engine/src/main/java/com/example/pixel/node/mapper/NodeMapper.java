package com.example.pixel.node.mapper;

import com.example.pixel.node.dto.NodeConfigurationDto;
import com.example.pixel.node.entity.NodeConfigurationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NodeMapper {
    NodeConfigurationDto toDto(NodeConfigurationEntity node);
}
