package com.drfirst.bblt.session1.service;

import com.drfirst.bblt.session1.config.ModelConfig;
import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Service
public class BedrockService {

    private static final Logger log = Logger.getLogger(BedrockService.class.getName());

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ModelConfig modelConfig;
    private final Map<String, ChatClient> modelChatClients = new ConcurrentHashMap<>();

    public BedrockService(BedrockRuntimeClient bedrockRuntimeClient, ModelConfig modelConfig) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
        this.modelConfig = modelConfig;
    }

    public ChatResponse processChat(ChatRequest request) {
        log.info("Processing chat request for model: " + request.modelId());

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        try {
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

        } catch (Exception e) {
            log.severe("Error processing chat request: " + e.getMessage());
            return ChatResponse.error(e.getMessage(), request.modelId());
        }
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
        return List.of("claude-3-sonnet", "claude-3-5-sonnet", "llama2-70b", "titan-express");
    }

}
