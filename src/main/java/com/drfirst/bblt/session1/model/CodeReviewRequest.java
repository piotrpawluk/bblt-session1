package com.drfirst.bblt.session1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeReviewRequest {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("modelId")
    private String modelId;

    public CodeReviewRequest() {}

    public CodeReviewRequest(String code, String language, String modelId) {
        this.code = code;
        this.language = language;
        this.modelId = modelId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getModelId() {
        return modelId != null ? modelId : "claude-3-7-sonnet";
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}