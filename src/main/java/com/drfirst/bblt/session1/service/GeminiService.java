package com.drfirst.bblt.session1.service;

import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Service for Google Vertex AI Gemini integration using Spring AI ChatClient.
 * Demonstrates the use of Spring AI with Google's Gemini models.
 */
@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    
    private final ChatClient geminiChatClient;
    private final VertexAiGeminiChatModel geminiChatModel;

    public GeminiService(VertexAiGeminiChatModel geminiChatModel) {
        this.geminiChatModel = geminiChatModel;
        this.geminiChatClient = ChatClient.builder(geminiChatModel).build();
        logger.info("GeminiService initialized with Vertex AI Gemini ChatClient");
    }

    /**
     * Simple chat completion using Gemini via Spring AI ChatClient
     */
    public ChatResponse chatCompletion(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Gemini chat request: message length={}", request.message().length());

            // Build the chat client call with options
            ChatClient.ChatClientRequestSpec clientRequest = geminiChatClient
                    .prompt()
                    .user(request.message());

            // Add system prompt if provided
            if (request.systemPrompt() != null && !request.systemPrompt().trim().isEmpty()) {
                clientRequest = clientRequest.system(request.systemPrompt());
            }

            // Execute the request with default options
            String response = clientRequest
                    .call()
                    .content();

            // Calculate metrics (Gemini-specific estimation)
            long responseTime = System.currentTimeMillis() - startTime;
            int inputTokens = estimateTokens(request.message() + (request.systemPrompt() != null ? request.systemPrompt() : ""));
            int outputTokens = estimateTokens(response);
            double estimatedCost = calculateGeminiCost(inputTokens, outputTokens);

            ChatResponse.ModelPerformanceMetrics metrics = ChatResponse.ModelPerformanceMetrics.create(
                    "gemini-2.5-flash", responseTime, inputTokens, outputTokens, estimatedCost, "complete"
            );

            logger.info("Gemini chat success: tokens={}, cost=${}, time={}ms", 
                       metrics.totalTokens(), metrics.estimatedCost(), responseTime);

            return ChatResponse.success(response, "gemini-2.5-flash", metrics);

        } catch (Exception e) {
            logger.error("Error with Gemini chat: {}", e.getMessage(), e);
            return ChatResponse.error("Gemini chat failed: " + e.getMessage(), "gemini-2.5-flash");
        }
    }

    /**
     * Streaming chat with Gemini using Spring AI ChatClient
     */
    public Flux<String> chatStream(ChatRequest request) {
        try {
            logger.info("Gemini stream request: message length={}", request.message().length());

            // Build the streaming chat client call
            ChatClient.ChatClientRequestSpec clientRequest = geminiChatClient
                    .prompt()
                    .user(request.message());

            // Add system prompt if provided
            if (request.systemPrompt() != null && !request.systemPrompt().trim().isEmpty()) {
                clientRequest = clientRequest.system(request.systemPrompt());
            }

            // Execute streaming request with default options
            return clientRequest
                    .stream()
                    .content()
                    .doOnNext(chunk -> logger.debug("Gemini stream chunk: {}", chunk))
                    .doOnComplete(() -> logger.info("Gemini stream completed"))
                    .doOnError(error -> logger.error("Gemini stream error: {}", error.getMessage()));

        } catch (Exception e) {
            logger.error("Error with Gemini stream: {}", e.getMessage(), e);
            return Flux.error(e);
        }
    }

    /**
     * Advanced prompt engineering with Gemini
     */
    public ChatResponse promptEngineering(String template, Map<String, Object> variables) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Gemini prompt engineering with template: {}", template);

            // Create prompt template
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Prompt prompt = promptTemplate.create(variables);

            // Execute with ChatClient
            String response = geminiChatClient
                    .prompt(prompt)
                    .call()
                    .content();

            // Calculate metrics
            long responseTime = System.currentTimeMillis() - startTime;
            int inputTokens = estimateTokens(template + variables.toString());
            int outputTokens = estimateTokens(response);
            double estimatedCost = calculateGeminiCost(inputTokens, outputTokens);

            ChatResponse.ModelPerformanceMetrics metrics = ChatResponse.ModelPerformanceMetrics.create(
                    "gemini-2.5-flash", responseTime, inputTokens, outputTokens, estimatedCost, "complete"
            );

            logger.info("Gemini prompt engineering success: tokens={}, cost=${}", 
                       metrics.totalTokens(), metrics.estimatedCost());

            return ChatResponse.success(response, "gemini-2.5-flash", metrics);

        } catch (Exception e) {
            logger.error("Error with Gemini prompt engineering: {}", e.getMessage(), e);
            return ChatResponse.error("Gemini prompt engineering failed: " + e.getMessage(), "gemini-2.5-flash");
        }
    }

    /**
     * Get model information and capabilities
     */
    public Map<String, Object> getModelInfo() {
        return Map.of(
            "provider", "Google Vertex AI",
            "model", "Gemini 2.5 Flash",
            "capabilities", Map.of(
                "chat", true,
                "streaming", true,
                "function_calling", true,
                "multimodal", true,
                "reasoning", true
            ),
            "pricing", Map.of(
                "input_tokens_per_million", 0.075, // $0.075 per 1M input tokens
                "output_tokens_per_million", 0.30,  // $0.30 per 1M output tokens
                "note", "Pricing for Gemini 2.5 Flash preview"
            ),
            "limits", Map.of(
                "max_output_tokens", 8192,
                "context_window", 2097152, // 2M tokens
                "rate_limits", "Varies by region and quota"
            ),
            "vs_bedrock", Map.of(
                "integration", "Spring AI ChatClient (same as Bedrock)",
                "authentication", "Google Cloud Service Account (vs AWS credentials)",
                "strengths", "Latest model, large context window, multimodal capabilities"
            )
        );
    }

    /**
     * Compare with AWS Bedrock models
     */
    public ChatResponse compareWithBedrock(String topic) {
        String template = """
            Compare Google Gemini 2.5 Flash with AWS Bedrock models for the following topic: {topic}
            
            Please provide a detailed comparison covering:
            1. Model capabilities and strengths
            2. Integration differences (Spring AI perspective)
            3. Pricing considerations
            4. Use case recommendations
            5. Performance characteristics
            
            Focus on practical insights for developers choosing between these platforms.
            """;

        return promptEngineering(template, Map.of("topic", topic));
    }

    /**
     * Estimate token count (rough approximation for Gemini)
     */
    private int estimateTokens(String text) {
        // Gemini uses SentencePiece tokenization, roughly 1 token ≈ 3-4 characters
        return text.length() / 3;
    }

    /**
     * Calculate estimated cost for Gemini usage
     */
    private double calculateGeminiCost(int inputTokens, int outputTokens) {
        // Gemini 2.5 Flash pricing (preview rates)
        double inputCost = (inputTokens / 1_000_000.0) * 0.075;   // $0.075 per 1M input tokens
        double outputCost = (outputTokens / 1_000_000.0) * 0.30;  // $0.30 per 1M output tokens
        return inputCost + outputCost;
    }
}