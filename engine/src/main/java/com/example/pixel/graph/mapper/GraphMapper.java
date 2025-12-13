package com.example.pixel.graph.mapper;


import com.example.pixel.graph.dto.GraphDto;
import com.example.pixel.graph.entity.GraphEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GraphMapper {
    @Mapping(source = "graphId", target = "id")
    GraphDto toDto(GraphEntity graphEntity);
}
