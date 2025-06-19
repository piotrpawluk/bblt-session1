package com.drfirst.bblt.session1.service;

import com.drfirst.bblt.session1.config.ModelConfig;
import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Service
public class BedrockService implements ModelInvoker {

    private static final Logger log = Logger.getLogger(BedrockService.class.getName());

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ModelConfig modelConfig;
    private final BedrockErrorHandler errorHandler;
    private final Map<String, ChatClient> modelChatClients = new ConcurrentHashMap<>();

    public BedrockService(BedrockRuntimeClient bedrockRuntimeClient, 
                         ModelConfig modelConfig,
                         BedrockErrorHandler errorHandler) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
        this.modelConfig = modelConfig;
        this.errorHandler = errorHandler;
    }

    @PostConstruct
    public void init() {
        // Set up circular dependency after construction
        errorHandler.setModelInvoker(this);
        log.info("BedrockService initialized with error handler and fallback capabilities");
    }

    @Retryable(
        retryFor = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public ChatResponse processChat(ChatRequest request) {
        log.info("Processing chat request for model: " + request.modelId() + " with enhanced error handling");

        // Check circuit breaker
        Map<String, String> cbStatus = errorHandler.getCircuitBreakerStatus();
        if (cbStatus.containsKey(request.modelId()) && cbStatus.get(request.modelId()).contains("OPEN")) {
            log.warning("Circuit breaker is OPEN for model: " + request.modelId());
            return ChatResponse.error(
                "⚠️ Model " + request.modelId() + " is temporarily unavailable (circuit breaker is open). Please try again later.",
                request.modelId()
            );
        }

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        try {
            ModelConfig.ModelProperties modelProps = modelConfig.getModels().get(request.modelId());
            if (modelProps == null) {
                throw new IllegalArgumentException("Unknown model: " + request.modelId());
            }
            
            log.info(modelProps.toString());
            ChatClient chatClient = getOrCreateChatClient(request.modelId(), modelProps);
            String promptText = buildPromptText(request);

            String response = chatClient
                    .prompt(promptText)
                    .call()
                    .content();

            long endTime = System.currentTimeMillis();
            
            // Record success
            recordModelSuccess(request.modelId());

            return buildChatResponse(request, response, startTime, endTime, requestId);

        } catch (Exception e) {
            // Record failure for circuit breaker
            recordModelFailure(request.modelId(), e);
            
            log.severe("Error processing chat request: " + e.getMessage());
            throw new RuntimeException("Model invocation failed: " + e.getMessage(), e);
        }
    }

    @Recover
    public ChatResponse recover(RuntimeException ex, ChatRequest request) {
        log.severe("All retry attempts exhausted for model: " + request.modelId());
        
        // Try a fallback model
        String fallbackModel = getFallbackModel(request.modelId());
        if (fallbackModel != null && !fallbackModel.equals(request.modelId())) {
            log.info("Attempting fallback from " + request.modelId() + " to " + fallbackModel);
            
            ChatRequest fallbackRequest = new ChatRequest(
                request.message(),
                request.systemPrompt(),
                fallbackModel,
                request.maxTokens(),
                request.temperature(),
                request.topP(),
                request.topK(),
                request.stream(),
                request.includeMetrics()
            );
            
            try {
                ChatResponse fallbackResponse = processDirectCall(fallbackRequest);
                if (fallbackResponse.isSuccess()) {
                    return ChatResponse.success(
                        "⚠️ Fallback used: " + request.modelId() + " → " + fallbackModel + "\n\n" + fallbackResponse.content(),
                        fallbackModel,
                        fallbackResponse.metrics()
                    );
                }
            } catch (Exception e) {
                log.warning("Fallback model " + fallbackModel + " also failed: " + e.getMessage());
            }
        }
        
        return ChatResponse.error(
            "All models unavailable. Original error: " + ex.getMessage() + ". Please try again later.",
            request.modelId()
        );
    }

    public Flux<String> processStreamChat(ChatRequest request) {
        log.info("Processing streaming chat request for model: " + request.modelId());

        try {
            ModelConfig.ModelProperties modelProps = modelConfig.getModels().get(request.modelId());
            if (modelProps == null) {
                return Flux.just("Error: Unknown model: " + request.modelId());
            }

            ChatClient chatClient = getOrCreateChatClient(request.modelId(), modelProps);
            String promptText = buildPromptText(request);

            return chatClient
                    .prompt(promptText)
                    .stream()
                    .content()
                    .doOnNext(content -> log.fine("Streaming content: " + content))
                    .doOnError(error -> log.severe("Error in streaming: " + error.getMessage()))
                    .onErrorReturn("Error occurred during streaming: " + request.modelId());

        } catch (Exception e) {
            log.severe("Error setting up streaming chat: " + e.getMessage());
            return Flux.just("Error: " + e.getMessage());
        }
    }

    public CompletableFuture<List<ChatResponse.ModelPerformanceMetrics>> compareModels(String message, List<String> modelIds) {
        log.info("Comparing models: " + modelIds + " with message: " + message);

        List<CompletableFuture<ChatResponse.ModelPerformanceMetrics>> futures = modelIds.stream()
                .map(modelId -> CompletableFuture.supplyAsync(() -> {
                    ChatRequest request = new ChatRequest(
                            message, null, modelId, 1000, 0.7, 0.9, 40, false, true
                    );

                    ChatResponse response = processChat(request);

                    return ChatResponse.ModelPerformanceMetrics.create(
                            modelId,
                            response.metrics() != null ? response.metrics().responseTimeMs() : 0,
                            response.metrics() != null ? response.metrics().inputTokens() : 0,
                            response.metrics() != null ? response.metrics().outputTokens() : 0,
                            response.metrics() != null ? response.metrics().estimatedCost() : 0,
                            response.metrics() != null ? response.metrics().finishReason() : "unknown"
                    );
                }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    private ChatClient getOrCreateChatClient(String modelId, ModelConfig.ModelProperties modelProps) {
        return modelChatClients.computeIfAbsent(modelId, k -> {
            ChatModel chatModel = createChatModel(modelProps);
            return ChatClient.builder(chatModel).build();
        });
    }

    private ChatModel createChatModel(ModelConfig.ModelProperties modelProps) {
        String actualModelId = modelProps.getModelId();

        return BedrockProxyChatModel.builder()
                .bedrockRuntimeClient(bedrockRuntimeClient)
                .defaultOptions(
                        ToolCallingChatOptions.builder()
                                .model(actualModelId)
                                .temperature(modelProps.getTemperature())
                                .topP(modelProps.getTopP())
                                .maxTokens(modelProps.getMaxTokens())
                                .build()
                )
                .build();
    }

    private String buildPromptText(ChatRequest request) {
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            return "System: " + request.systemPrompt() + "\n\nUser: " + request.message();
        } else {
            return request.message();
        }
    }

    private ChatResponse buildChatResponse(ChatRequest request, String content,
                                           long startTime, long endTime, String requestId) {
        long responseTime = endTime - startTime;

        // Mock token usage for now - in real implementation this would come from the model response
        int inputTokens = request.message().length() / 4; // Rough estimation
        int outputTokens = content.length() / 4;
        int totalTokens = inputTokens + outputTokens;

        // Calculate cost estimation
        double estimatedCost = calculateCost(request.modelId(), inputTokens, outputTokens);

        ChatResponse.ModelPerformanceMetrics metrics = ChatResponse.ModelPerformanceMetrics.create(
                request.modelId(),
                responseTime,
                inputTokens,
                outputTokens,
                estimatedCost,
                "STOP"
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", request.modelId());
        metadata.put("temperature", request.temperature());
        metadata.put("maxTokens", request.maxTokens());
        metadata.put("topP", request.topP());
        metadata.put("topK", request.topK());
        metadata.put("response", content);

        return new ChatResponse(
                content,
                request.modelId(),
                request.includeMetrics() ? metrics : null,
                metadata,
                LocalDateTime.now(),
                requestId,
                true,
                null
        );
    }

    private double calculateCost(String modelId, int inputTokens, int outputTokens) {
        ModelConfig.ModelProperties modelProps = modelConfig.getModels().get(modelId);
        if (modelProps == null) {
            return 0.0;
        }

        double inputCost = (inputTokens / 1000.0) * modelProps.getCostPer1kInputTokens();
        double outputCost = (outputTokens / 1000.0) * modelProps.getCostPer1kOutputTokens();

        return inputCost + outputCost;
    }

    private String getModelDisplayName(String modelId) {
        ModelConfig.ModelProperties modelProps = modelConfig.getModels().get(modelId);
        return modelProps != null ? modelProps.getDisplayName() : modelId;
    }

    public Map<String, Object> getModelInfo(String modelId) {
        ModelConfig.ModelProperties modelProps = modelConfig.getModels().get(modelId);
        if (modelProps == null) {
            return Map.of("error", "Model not found: " + modelId);
        }

        Map<String, Object> info = new HashMap<>();
        info.put("modelId", modelProps.getModelId());
        info.put("displayName", modelProps.getDisplayName());
        info.put("provider", modelProps.getProvider());
        info.put("maxTokens", modelProps.getMaxTokens());
        info.put("contextWindow", modelProps.getContextWindow());
        info.put("costPer1kInputTokens", modelProps.getCostPer1kInputTokens());
        info.put("costPer1kOutputTokens", modelProps.getCostPer1kOutputTokens());
        info.put("streamingSupported", modelProps.isStreamingSupported());
        info.put("rateLimits", modelProps.getRateLimits());

        return info;
    }

    public List<String> getAvailableModels() {
        return List.of("claude-3-7-sonnet", "claude-4-opus", "nova-pro", "titan-express");
    }

    /**
     * Get circuit breaker status for monitoring and diagnostics
     */
    public Map<String, String> getCircuitBreakerStatus() {
        return errorHandler.getCircuitBreakerStatus();
    }

    /**
     * Record successful model invocation for circuit breaker
     */
    private void recordModelSuccess(String modelId) {
        // Delegate to error handler
        try {
            errorHandler.recordSuccess(modelId);
        } catch (Exception e) {
            log.warning("Failed to record model success: " + e.getMessage());
        }
    }

    /**
     * Record failed model invocation for circuit breaker
     */
    private void recordModelFailure(String modelId, Exception error) {
        // Delegate to error handler
        try {
            errorHandler.recordFailure(modelId);
            log.warning("Recorded failure for model " + modelId + ": " + error.getMessage());
        } catch (Exception e) {
            log.warning("Failed to record model failure: " + e.getMessage());
        }
    }

    /**
     * Get fallback model for the given model
     */
    private String getFallbackModel(String modelId) {
        switch (modelId) {
            case "claude-4-opus":
                return "claude-3-7-sonnet";
            case "claude-3-7-sonnet":
                return "nova-pro";
            case "nova-pro":
                return "titan-express";
            case "titan-express":
                return "claude-3-7-sonnet";
            default:
                return "claude-3-7-sonnet";
        }
    }

    /**
     * Process chat without retry logic for fallback scenarios
     */
    private ChatResponse processDirectCall(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        ModelConfig.ModelProperties modelProps = modelConfig.getModels().get(request.modelId());
        if (modelProps == null) {
            throw new IllegalArgumentException("Unknown model: " + request.modelId());
        }

        ChatClient chatClient = getOrCreateChatClient(request.modelId(), modelProps);
        String promptText = buildPromptText(request);

        String response = chatClient
                .prompt(promptText)
                .call()
                .content();

        long endTime = System.currentTimeMillis();

        return buildChatResponse(request, response, startTime, endTime, requestId);
    }

    /**
     * Implementation of ModelInvoker interface for fallback mechanism
     * This method is called by BedrockErrorHandler to invoke models directly
     */
    @Override
    public ChatResponse invokeModelDirect(ChatRequest request) {
        log.info("Direct model invocation for fallback: " + request.modelId());
        
        try {
            return processDirectCall(request);
        } catch (Exception e) {
            log.severe("Direct model invocation failed: " + e.getMessage());
            return ChatResponse.error(
                "Direct invocation failed: " + e.getMessage(), 
                request.modelId()
            );
        }
    }

}
