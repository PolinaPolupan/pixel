package com.example.pixel.node;

import com.example.pixel.exception.InvalidNodeParameter;
import lombok.Getter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
public class NodeReference {
    private static final Pattern NODE_REF_PATTERN = Pattern.compile("@node:(\\d+):(\\w+)");

    private final String reference;

    @JsonIgnore
    private transient Matcher matcher;

    @JsonIgnore
    private transient Pattern nodeRefPattern;

    @JsonCreator
    public NodeReference(@JsonProperty("reference") String reference) {
        this.reference = reference;
        this.nodeRefPattern = NODE_REF_PATTERN;
        this.matcher = nodeRefPattern.matcher(reference);

        if (!matcher.matches()) {
            throw new InvalidNodeParameter("Invalid node reference format: " + reference);
        }
    }

    @JsonIgnore
    public Long getNodeId() {
        if (matcher == null || !matcher.matches()) {
            matcher = NODE_REF_PATTERN.matcher(reference);
            if (!matcher.matches()) {
                throw new InvalidNodeParameter("Invalid node reference format: " + reference);
            }
        }
        return Long.parseLong(matcher.group(1));
    }

    @JsonIgnore
    public String getOutputName() {
        if (matcher == null || !matcher.matches()) {
            matcher = NODE_REF_PATTERN.matcher(reference);
            if (!matcher.matches()) {
                throw new InvalidNodeParameter("Invalid node reference format: " + reference);
            }
        }
        return matcher.group(2);
    }

    @Override
    public String toString() {
        return reference;
    }
}
