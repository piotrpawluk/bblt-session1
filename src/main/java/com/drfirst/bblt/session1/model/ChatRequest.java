package com.drfirst.bblt.session1.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record ChatRequest(
    @NotBlank(message = "Message cannot be blank")
    String message,
    
    String systemPrompt,
    
    String modelId,
    
    @Min(value = 1, message = "Max tokens must be at least 1")
    @Max(value = 8192, message = "Max tokens cannot exceed 8192")
    Integer maxTokens,
    
    @DecimalMin(value = "0.0", message = "Temperature must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Temperature cannot exceed 1.0")
    Double temperature,
    
    @DecimalMin(value = "0.0", message = "Top P must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Top P cannot exceed 1.0")
    Double topP,
    
    @Min(value = 1, message = "Top K must be at least 1")
    @Max(value = 500, message = "Top K cannot exceed 500")
    Integer topK,
    
    Boolean stream,
    Boolean includeMetrics
) {
    // Default constructor with commonly used defaults
    public ChatRequest(String message) {
        this(message, null, "claude-3-7-sonnet", 1000, 0.7, 0.9, 40, false, false);
    }
    
    // Default values for null parameters
    public String modelId() {
        return modelId != null ? modelId : "claude-3-7-sonnet";
    }
    
    public Integer maxTokens() {
        return maxTokens != null ? maxTokens : 1000;
    }
    
    public Double temperature() {
        return temperature != null ? temperature : 0.7;
    }
    
    public Double topP() {
        return topP != null ? topP : 0.9;
    }
    
    public Integer topK() {
        return topK != null ? topK : 40;
    }
    
    public Boolean stream() {
        return stream != null ? stream : false;
    }
    
    public Boolean includeMetrics() {
        return includeMetrics != null ? includeMetrics : false;
    }
}