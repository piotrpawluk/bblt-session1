package com.drfirst.bblt.session1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FewShotRequest {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("domain")
    private String domain;
    
    @JsonProperty("modelId")
    private String modelId;

    public FewShotRequest() {}

    public FewShotRequest(String query, String domain, String modelId) {
        this.query = query;
        this.domain = domain;
        this.modelId = modelId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getModelId() {
        return modelId != null ? modelId : "claude-3-7-sonnet";
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}