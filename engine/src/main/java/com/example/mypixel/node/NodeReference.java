package com.example.mypixel.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Getter
public class NodeReference {
    private final Pattern nodeRefPattern = Pattern.compile("@node:(\\d+):(\\w+)");
    private final String reference;
    private final Matcher matcher;

    public NodeReference(String reference) {
        matcher = nodeRefPattern.matcher(reference);
        if (!matcher.matches()) {
            throw new InvalidNodeParameter("Invalid node reference format: " + reference);
        }

        this.reference = reference;
    }

    public Long getNodeId() {
        return Long.parseLong(matcher.group(1));
    }

    public String getOutputName() { return matcher.group(2); }
}
