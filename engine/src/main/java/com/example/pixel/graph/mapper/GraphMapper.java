package com.example.pixel.graph.mapper;


import com.example.pixel.graph.dto.GraphPayload;
import com.example.pixel.graph.entity.GraphEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GraphMapper {
    GraphPayload toDto(GraphEntity graphEntity);
}
