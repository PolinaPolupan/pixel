package com.example.pixel.node;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;


public class InputDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isTextual() && node.asText().startsWith("@node:")) {
            return new NodeReference(node.asText());
        }

        if (node.isNumber()) {
            validateNumber(node, p.currentName());
        }

        return p.getCodec().treeToValue(node, Object.class);
    }

    private void validateNumber(JsonNode node, String fieldName) {
        try {
            double value;
            String originalValue;

            if (node.isBigInteger()) {
                BigInteger bigInt = node.bigIntegerValue();
                value = bigInt.doubleValue();
                originalValue = bigInt.toString();
            } else if (node.isBigDecimal()) {
                BigDecimal bigDec = node.decimalValue();
                value = bigDec.doubleValue();
                originalValue = bigDec.toString();
            } else {
                value = node.doubleValue();
                originalValue = node.asText();
            }

            if (Double.isInfinite(value) || Double.isNaN(value)) {
                throw new IllegalArgumentException("Value too large or not representable for '" +
                        fieldName + "': " + originalValue);
            }

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Invalid numeric value for '" + fieldName + "'", e);
        }
    }
}
