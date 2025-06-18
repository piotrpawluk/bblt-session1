package com.drfirst.bblt.session1.controller;

import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import com.drfirst.bblt.session1.service.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Controller for Google Vertex AI Gemini integration using Spring AI ChatClient.
 * Demonstrates the use of Spring AI with Google's latest Gemini models.
 */
@RestController
@RequestMapping("/api/gemini")
@Validated
@Tag(name = "Google Vertex AI Gemini", description = "Spring AI integration with Google Vertex AI Gemini models")
public class GeminiController {

    private static final Logger logger = LoggerFactory.getLogger(GeminiController.class);
    
    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Chat with Gemini 2.5 Flash",
        description = "Send a message to Google's Gemini 2.5 Flash model using Spring AI ChatClient. " +
                     "Demonstrates the latest Google AI capabilities including advanced reasoning, " +
                     "large context window (2M tokens), and multimodal understanding."
    )
    public ResponseEntity<ChatResponse> chatWithGemini(
            @Valid @RequestBody ChatRequest request) {
        
        logger.info("Gemini chat request: message length={}", request.message().length());

        ChatResponse response = geminiService.chatCompletion(request);
        
        if (response.isSuccess()) {
            logger.info("Gemini chat success: tokens={}, cost=${}", 
                       response.metrics().totalTokens(), response.metrics().estimatedCost());
            return ResponseEntity.ok(response);
        } else {
            logger.error("Gemini chat error: {}", response.errorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
        summary = "Stream chat with Gemini 2.5 Flash",
        description = "Stream a conversation with Gemini 2.5 Flash model using Spring AI ChatClient. " +
                     "Returns real-time streaming responses for interactive experiences."
    )
    public Flux<String> streamChatWithGemini(
            @Valid @RequestBody ChatRequest request) {
        
        logger.info("Gemini stream request: message length={}", request.message().length());
        
        return geminiService.chatStream(request)
                .doOnComplete(() -> logger.info("Gemini stream completed"))
                .doOnError(error -> logger.error("Gemini stream error: {}", error.getMessage()));
    }

    @PostMapping(value = "/compare-bedrock", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Compare Gemini with AWS Bedrock models",
        description = "Ask Gemini to compare itself with AWS Bedrock models on a specific topic. " +
                     "Provides insights into choosing between Google AI and AWS AI services."
    )
    public ResponseEntity<ChatResponse> compareWithBedrock(
            @Parameter(description = "Topic to compare (e.g., 'performance', 'pricing', 'capabilities')")
            @RequestParam(defaultValue = "general capabilities") String topic) {
        
        logger.info("Gemini comparison request for topic: {}", topic);

        ChatResponse response = geminiService.compareWithBedrock(topic);
        
        if (response.isSuccess()) {
            logger.info("Gemini comparison success: tokens={}", response.metrics().totalTokens());
            return ResponseEntity.ok(response);
        } else {
            logger.error("Gemini comparison error: {}", response.errorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/info")
    @Operation(
        summary = "Get Gemini model information",
        description = "Returns detailed information about the Gemini 2.5 Flash model including " +
                     "capabilities, pricing, limits, and comparison with AWS Bedrock"
    )
    public ResponseEntity<Map<String, Object>> getGeminiInfo() {
        Map<String, Object> info = geminiService.getModelInfo();
        return ResponseEntity.ok(info);
    }

    @PostMapping(value = "/prompt-engineering", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Advanced prompt engineering with Gemini",
        description = "Demonstrate advanced prompt engineering techniques using Spring AI PromptTemplate " +
                     "with Gemini 2.5 Flash for complex reasoning tasks"
    )
    public ResponseEntity<ChatResponse> promptEngineering(
            @RequestBody Map<String, Object> request) {
        
        String template = (String) request.get("template");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.getOrDefault("variables", Map.of());
        
        if (template == null || template.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ChatResponse.error("Template is required for prompt engineering", "gemini-2.5-flash")
            );
        }

        logger.info("Gemini prompt engineering request with {} variables", variables.size());

        ChatResponse response = geminiService.promptEngineering(template, variables);
        
        if (response.isSuccess()) {
            logger.info("Gemini prompt engineering success: tokens={}", response.metrics().totalTokens());
            return ResponseEntity.ok(response);
        } else {
            logger.error("Gemini prompt engineering error: {}", response.errorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/examples")
    @Operation(
        summary = "Get example requests for Gemini",
        description = "Returns example request payloads for testing Gemini endpoints"
    )
    public ResponseEntity<Map<String, Object>> getExamples() {
        Map<String, Object> examples = Map.of(
            "chat_example", Map.of(
                "message", "Explain quantum computing in simple terms",
                "systemPrompt", "You are a helpful AI assistant specializing in complex technical topics",
                "modelId", "gemini-2.5-flash",
                "maxTokens", 2000,
                "temperature", 0.7,
                "topP", 0.4,
                "stream", false,
                "includeMetrics", true
            ),
            "stream_example", Map.of(
                "message", "Write a short story about AI and humans working together",
                "systemPrompt", "You are a creative writer with expertise in science fiction",
                "maxTokens", 1500,
                "temperature", 0.8,
                "topP", 0.6
            ),
            "prompt_engineering_example", Map.of(
                "template", """
                    You are an expert {role} with {experience} years of experience.
                    
                    Please analyze the following {subject} and provide:
                    1. Key insights
                    2. Recommendations
                    3. Potential challenges
                    4. Best practices
                    
                    Subject: {topic}
                    
                    Focus your analysis on practical, actionable advice.
                    """,
                "variables", Map.of(
                    "role", "software architect",
                    "experience", "10",
                    "subject", "system design",
                    "topic", "Building scalable microservices architecture"
                )
            ),
            "endpoints", Map.of(
                "chat", "/api/gemini/chat",
                "stream", "/api/gemini/stream",
                "compare", "/api/gemini/compare-bedrock?topic=performance",
                "info", "/api/gemini/info",
                "prompt_engineering", "/api/gemini/prompt-engineering"
            ),
            "notes", Map.of(
                "spring_ai_integration", "Uses Spring AI ChatClient for seamless integration",
                "model_features", "Gemini 2.5 Flash with 2M token context window",
                "multimodal", "Supports text, images, and reasoning tasks",
                "vs_bedrock", "Compare endpoints show differences with AWS models"
            )
        );
        
        return ResponseEntity.ok(examples);
    }
}