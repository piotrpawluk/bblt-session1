package com.drfirst.bblt.session1.service;

import com.drfirst.bblt.session1.config.ModelConfig;
import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for direct AWS Bedrock SDK calls without Spring AI abstraction.
 * Demonstrates raw JSON payload construction and response parsing.
 */
@Service
public class DirectBedrockService {

    private static final Logger logger = LoggerFactory.getLogger(DirectBedrockService.class);
    
    private final BedrockRuntimeClient bedrockClient;
    private final ModelConfig modelConfig;
    private final BedrockErrorHandler errorHandler;
    private final ObjectMapper objectMapper;

    public DirectBedrockService(BedrockRuntimeClient bedrockClient, 
                               ModelConfig modelConfig,
                               BedrockErrorHandler errorHandler) {
        this.bedrockClient = bedrockClient;
        this.modelConfig = modelConfig;
        this.errorHandler = errorHandler;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Invoke Claude model directly using AWS Bedrock SDK with enhanced error handling
     */
    public ChatResponse invokeClaudeDirect(ChatRequest request) {
        logger.info("Invoking Claude model directly with enhanced error handling: {}", request.modelId());
        
        try {
            // Use the same error handler for consistency
            return errorHandler.invokeWithRetry(request);
            
        } catch (Exception e) {
            logger.error("Direct Claude invocation failed after all retries: {}", e.getMessage(), e);
            return ChatResponse.error(
                "Direct Claude SDK call failed after retries: " + e.getMessage(), 
                request.modelId()
            );
        }
    }

    /**
     * Invoke Nova Pro model directly using AWS Bedrock SDK with enhanced error handling
     */
    public ChatResponse invokeNovaProDirect(ChatRequest request) {
        logger.info("Invoking Nova Pro model directly with enhanced error handling: {}", request.modelId());
        
        try {
            // Use the same error handler for consistency
            return errorHandler.invokeWithRetry(request);
            
        } catch (Exception e) {
            logger.error("Direct Nova Pro invocation failed after all retries: {}", e.getMessage(), e);
            return ChatResponse.error(
                "Direct Nova Pro SDK call failed after retries: " + e.getMessage(), 
                request.modelId()
            );
        }
    }

    /**
     * Build Claude-specific payload according to Anthropic's format
     */
    private Map<String, Object> buildClaudePayload(ChatRequest request, ModelConfig.ModelProperties modelProps) {
        Map<String, Object> payload = new HashMap<>();
        
        // Claude expects messages in a specific format
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", request.message());
        
        payload.put("messages", List.of(message));
        payload.put("max_tokens", request.maxTokens());
        payload.put("temperature", request.temperature());
        payload.put("top_p", request.topP());
        payload.put("top_k", request.topK());
        
        // Add system prompt if provided
        if (request.systemPrompt() != null && !request.systemPrompt().trim().isEmpty()) {
            payload.put("system", request.systemPrompt());
        }
        
        payload.put("anthropic_version", "bedrock-2023-05-31");
        
        return payload;
    }

    /**
     * Build Nova Pro-specific payload according to Amazon's format
     */
    private Map<String, Object> buildNovaProPayload(ChatRequest request, ModelConfig.ModelProperties modelProps) {
        Map<String, Object> payload = new HashMap<>();
        
        // Nova Pro expects Amazon's format
        String prompt = request.message();
        if (request.systemPrompt() != null && !request.systemPrompt().trim().isEmpty()) {
            prompt = request.systemPrompt() + "\n\nHuman: " + request.message() + "\n\nAssistant:";
        } else {
            prompt = "Human: " + request.message() + "\n\nAssistant:";
        }
        
        payload.put("prompt", prompt);
        payload.put("max_gen_len", request.maxTokens());
        payload.put("temperature", request.temperature());
        payload.put("top_p", request.topP());
        
        return payload;
    }

    /**
     * Parse Claude response
     */
    private ChatResponse parseClaudeResponse(String responseBody, String modelId, long startTime, ModelConfig.ModelProperties modelProps) {
        try {
            JsonNode response = objectMapper.readTree(responseBody);
            
            // Extract content from Claude response
            String content = response.path("content").get(0).path("text").asText();
            
            // Extract usage metrics
            JsonNode usage = response.path("usage");
            int inputTokens = usage.path("input_tokens").asInt(0);
            int outputTokens = usage.path("output_tokens").asInt(0);
            
            // Calculate metrics
            long responseTime = System.currentTimeMillis() - startTime;
            double estimatedCost = calculateCost(inputTokens, outputTokens, modelProps);
            String finishReason = response.path("stop_reason").asText("complete");
            
            ChatResponse.ModelPerformanceMetrics metrics = ChatResponse.ModelPerformanceMetrics.create(
                    modelId, responseTime, inputTokens, outputTokens, estimatedCost, finishReason
            );
            
            return ChatResponse.success(content, modelId, metrics);
            
        } catch (Exception e) {
            logger.error("Error parsing Claude response: {}", e.getMessage(), e);
            return ChatResponse.error("Failed to parse Claude response: " + e.getMessage(), modelId);
        }
    }

    /**
     * Parse Nova Pro response
     */
    private ChatResponse parseNovaProResponse(String responseBody, String modelId, long startTime, ModelConfig.ModelProperties modelProps) {
        try {
            JsonNode response = objectMapper.readTree(responseBody);
            
            // Extract content from Nova Pro response
            String content = response.path("generation").asText();
            
            // Nova Pro provides detailed usage metrics
            // We'll estimate based on content length
            int inputTokens = estimateTokens(modelId + " input");
            int outputTokens = estimateTokens(content);
            
            // Calculate metrics
            long responseTime = System.currentTimeMillis() - startTime;
            double estimatedCost = calculateCost(inputTokens, outputTokens, modelProps);
            String finishReason = response.path("stop_reason").asText("complete");
            
            ChatResponse.ModelPerformanceMetrics metrics = ChatResponse.ModelPerformanceMetrics.create(
                    modelId, responseTime, inputTokens, outputTokens, estimatedCost, finishReason
            );
            
            return ChatResponse.success(content, modelId, metrics);
            
        } catch (Exception e) {
            logger.error("Error parsing Nova Pro response: {}", e.getMessage(), e);
            return ChatResponse.error("Failed to parse Nova Pro response: " + e.getMessage(), modelId);
        }
    }

    /**
     * Calculate estimated cost based on token usage
     */
    private double calculateCost(int inputTokens, int outputTokens, ModelConfig.ModelProperties modelProps) {
        double inputCost = (inputTokens / 1000.0) * modelProps.getCostPer1kInputTokens();
        double outputCost = (outputTokens / 1000.0) * modelProps.getCostPer1kOutputTokens();
        return inputCost + outputCost;
    }

    /**
     * Rough estimation of token count (for models that don't provide exact counts)
     */
    private int estimateTokens(String text) {
        // Rough approximation: 1 token ≈ 4 characters
        return text.length() / 4;
    }
}