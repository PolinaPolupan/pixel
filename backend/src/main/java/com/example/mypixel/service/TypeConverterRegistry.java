package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.Vector2D;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class TypeConverterRegistry {
    private final Map<ParameterType, TypeConverter> converters = new HashMap<>();
    private final BatchProcessor batchProcessor;

    @PostConstruct
    public void initializeConverters() {
        converters.put(ParameterType.FLOAT, this::convertToFloat);
        converters.put(ParameterType.INT, this::convertToInt);
        converters.put(ParameterType.DOUBLE, this::convertToDouble);
        converters.put(ParameterType.STRING, this::convertToString);
        converters.put(ParameterType.VECTOR2D, this::convertToVector2D);
        converters.put(ParameterType.FILEPATH_ARRAY, this::convertToFilePathArray);
        converters.put(ParameterType.STRING_ARRAY, this::convertToStringArray);
    }

    public Object convert(Object value, Parameter parameter, FileHelper fileHelper) {
        if (value == null) {
            throw new InvalidNodeParameter("Cannot cast null to " + parameter.getType() + " type");
        }

        TypeConverter converter = converters.get(parameter.getType());
        if (converter == null) {
            throw new InvalidNodeParameter("No converter registered for type: " + parameter.getType());
        }

        return converter.convert(value, fileHelper);
    }

    private Object convertToFloat(Object value, FileHelper fileHelper) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to Float");
    }

    private Object convertToInt(Object value, FileHelper fileHelper) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to Integer");
    }

    private Object convertToDouble(Object value, FileHelper fileHelper) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to Double");
    }

    private Object convertToString(Object value, FileHelper fileHelper) {
        if (value instanceof String string) {
            return string;
        }
        throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to String");
    }

    private Object convertToVector2D(Object value, FileHelper fileHelper) {
        if (value instanceof Vector2D vector) {
            return vector;
        } else if (value instanceof Map map) {
            try {
                return Vector2D.fromMap((Map<String, Object>) map);
            } catch (ClassCastException e) {
                throw new InvalidNodeParameter("Invalid map structure for Vector2D conversion");
            }
        }
        throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to Vector2D");
    }

    private Object convertToFilePathArray(Object value, FileHelper fileHelper) {
        if (fileHelper == null) {
            throw new InvalidNodeParameter("FileHelper is required for FILEPATH_ARRAY conversion");
        }

        HashSet<String> files = new HashSet<>();
        if (value instanceof Collection<?> collection) {
            batchProcessor.processBatches(
                    collection,
                    item -> {
                        if (item instanceof String file) {
                            files.add(fileHelper.createDump(file));
                        } else {
                            throw new InvalidNodeParameter(
                                    "Invalid file path: expected String but got " +
                                            (item != null ? item.getClass().getSimpleName() : "null")
                            );
                        }
                    }
            );
            return files;
        }
        throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to FILEPATH_ARRAY");
    }

    private Object convertToStringArray(Object value, FileHelper fileHelper) {
        if (value instanceof List<?> list) {
            // Verify each element is a string
            for (Object item : list) {
                if (!(item instanceof String)) {
                    throw new InvalidNodeParameter("Expected List<String> but found non-string element: "
                            + (item != null ? item.getClass().getSimpleName() : "null"));
                }
            }
            return list;
        }
        throw new InvalidNodeParameter("Cannot convert " + value.getClass().getSimpleName() + " to STRING_ARRAY");
    }

    @FunctionalInterface
    private interface TypeConverter {
        Object convert(Object value, FileHelper fileHelper);
    }
}
