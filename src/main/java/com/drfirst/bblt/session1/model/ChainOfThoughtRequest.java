package com.drfirst.bblt.session1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChainOfThoughtRequest {
    
    @JsonProperty("problem")
    private String problem;
    
    @JsonProperty("modelId")
    private String modelId;

    public ChainOfThoughtRequest() {}

    public ChainOfThoughtRequest(String problem, String modelId) {
        this.problem = problem;
        this.modelId = modelId;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getModelId() {
        return modelId != null ? modelId : "claude-3-sonnet";
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}