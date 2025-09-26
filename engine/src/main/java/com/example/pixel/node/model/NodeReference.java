package com.example.pixel.node.model;

import com.example.pixel.common.exception.InvalidNodeInputException;
import lombok.Getter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class NodeReference {
    private static final Pattern NODE_REF_PATTERN = Pattern.compile("@node:(\\d+):(\\w+)");
    private final String reference;
    private transient Matcher matcher;
    private final transient Pattern nodeRefPattern;

    public NodeReference(String reference) {
        this.reference = reference;
        this.nodeRefPattern = NODE_REF_PATTERN;
        this.matcher = nodeRefPattern.matcher(reference);

        if (!matcher.matches()) {
            throw new InvalidNodeInputException("Invalid node reference format: " + reference);
        }
    }

    public Long getNodeId() {
        if (matcher == null || !matcher.matches()) {
            matcher = NODE_REF_PATTERN.matcher(reference);
            if (!matcher.matches()) {
                throw new InvalidNodeInputException("Invalid node reference format: " + reference);
            }
        }
        return Long.parseLong(matcher.group(1));
    }

    public String getOutputName() {
        if (matcher == null || !matcher.matches()) {
            matcher = NODE_REF_PATTERN.matcher(reference);
            if (!matcher.matches()) {
                throw new InvalidNodeInputException("Invalid node reference format: " + reference);
            }
        }
        return matcher.group(2);
    }

    @Override
    public String toString() {
        return reference;
    }
}
