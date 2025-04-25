package com.example.mypixel.model;

import com.example.mypixel.model.node.Node;

import java.util.Map;

public class BatchNodeWrapper extends Node {
    private final Node delegate;
    private final Map<String, Object> batchInputs;

    public BatchNodeWrapper(Node delegate, Map<String, Object> batchInputs) {
        super(delegate.getId(), delegate.getType(), null); // Null inputs as we'll override getInputs()
        this.delegate = delegate;
        this.batchInputs = batchInputs;
    }

    @Override
    public Map<String, Object> getInputs() {
        return batchInputs;
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return delegate.getInputTypes();
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return delegate.getOutputTypes();
    }

    @Override
    public Map<String, Object> exec() {
        return delegate.exec();
    }

    @Override
    public void validate() {
        delegate.validate();
    }
}
