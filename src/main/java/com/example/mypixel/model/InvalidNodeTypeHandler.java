package com.example.mypixel.model;

import com.example.mypixel.exception.InvalidNodeType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;


public class InvalidNodeTypeHandler extends DeserializationProblemHandler {
    @Override
    public JavaType handleUnknownTypeId(DeserializationContext ctx,
                                        JavaType baseType, String subTypeId,
                                        TypeIdResolver idResolver, String failureMsg) {
        throw new InvalidNodeType("Invalid node type: " + subTypeId);
    }
}