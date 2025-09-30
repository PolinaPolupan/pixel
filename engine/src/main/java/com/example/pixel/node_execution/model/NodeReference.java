package com.example.pixel.node_execution.model;

import com.example.pixel.common.exception.InvalidNodeInputException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class NodeReference {
    private static final Pattern NODE_REF_PATTERN = Pattern.compile("@node:(\\d+):(\\w+)");
    private static final String INVALID_NODE_REFERENCE_FORMAT_MESSAGE = "Invalid node reference format: ";

    private final String reference;
    @JsonIgnore
    private Matcher matcher;

    public NodeReference(String reference) {
        this.reference = reference;
        this.matcher = NODE_REF_PATTERN.matcher(reference);

        if (!matcher.matches()) {
            throw new InvalidNodeInputException(INVALID_NODE_REFERENCE_FORMAT_MESSAGE + reference);
        }
    }

    public Long getNodeId() {
        return Long.parseLong(getMatcherGroup(1));
    }

    public String getOutputName() {
        return getMatcherGroup(2);
    }

    private String getMatcherGroup(int group) {
        if (matcher == null || !matcher.matches()) {
            matcher = NODE_REF_PATTERN.matcher(reference);
            if (!matcher.matches()) {
                throw new InvalidNodeInputException(INVALID_NODE_REFERENCE_FORMAT_MESSAGE + reference);
            }
        }
        return matcher.group(group);
    }

    @Override
    public String toString() {
        return reference;
    }
}
