package com.drfirst.bblt.session1.model;

import java.time.LocalDateTime;
import java.util.Map;

public record ChatResponse(
    String content,
    String modelId,
    ModelPerformanceMetrics metrics,
    Map<String, Object> metadata,
    LocalDateTime timestamp,
    String requestId,
    boolean isSuccess,
    String errorMessage
) {
    
    public static ChatResponse success(String content, String modelId, ModelPerformanceMetrics metrics) {
        return new ChatResponse(
            content,
            modelId,
            metrics,
            null,
            LocalDateTime.now(),
            null,
            true,
            null
        );
    }
    
    public static ChatResponse error(String errorMessage, String modelId) {
        return new ChatResponse(
            null,
            modelId,
            null,
            null,
            LocalDateTime.now(),
            null,
            false,
            errorMessage
        );
    }
    
    public record ModelPerformanceMetrics(
        String modelId,
        long responseTimeMs,
        int inputTokens,
        int outputTokens,
        int totalTokens,
        double estimatedCost,
        String finishReason,
        double tokensPerSecond
    ) {
        
        public static ModelPerformanceMetrics create(
            String modelId,
            long responseTimeMs,
            int inputTokens,
            int outputTokens,
            double estimatedCost,
            String finishReason
        ) {
            int totalTokens = inputTokens + outputTokens;
            double tokensPerSecond = responseTimeMs > 0 ? (double) totalTokens / (responseTimeMs / 1000.0) : 0.0;
            
            return new ModelPerformanceMetrics(
                modelId,
                responseTimeMs,
                inputTokens,
                outputTokens,
                totalTokens,
                estimatedCost,
                finishReason,
                tokensPerSecond
            );
        }
    }
}