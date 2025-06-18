package com.drfirst.bblt.session1.controller;

import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import com.drfirst.bblt.session1.service.DirectBedrockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller demonstrating direct AWS Bedrock SDK usage without Spring AI abstraction.
 * This shows how to construct raw JSON payloads and parse responses manually.
 */
@RestController
@RequestMapping("/api/direct-bedrock")
@Validated
@Tag(name = "Direct Bedrock SDK", description = "Direct AWS Bedrock SDK examples without Spring AI")
public class DirectBedrockController {

    private static final Logger logger = LoggerFactory.getLogger(DirectBedrockController.class);
    
    private final DirectBedrockService directBedrockService;

    public DirectBedrockController(DirectBedrockService directBedrockService) {
        this.directBedrockService = directBedrockService;
    }

    @PostMapping(value = "/claude", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Chat with Claude using direct AWS Bedrock SDK",
        description = "Demonstrates direct AWS Bedrock SDK usage for Claude models without Spring AI abstraction. " +
                     "This endpoint constructs the raw JSON payload according to Anthropic's Claude format and " +
                     "manually parses the response, showing the underlying API structure."
    )
    public ResponseEntity<ChatResponse> chatWithClaudeDirect(
            @Valid @RequestBody ChatRequest request) {
        
        logger.info("Direct Claude SDK request: model={}, message length={}", 
                   request.modelId(), request.message().length());

        // Validate that it's a Claude model
        if (!request.modelId().contains("claude")) {
            return ResponseEntity.badRequest().body(
                ChatResponse.error("This endpoint only supports Claude models. Use model ID containing 'claude'.", request.modelId())
            );
        }

        ChatResponse response = directBedrockService.invokeClaudeDirect(request);
        
        if (response.isSuccess()) {
            logger.info("Direct Claude SDK success: tokens={}, cost=${}", 
                       response.metrics().totalTokens(), response.metrics().estimatedCost());
            return ResponseEntity.ok(response);
        } else {
            logger.error("Direct Claude SDK error: {}", response.errorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/llama", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Chat with Llama using direct AWS Bedrock SDK",
        description = "Demonstrates direct AWS Bedrock SDK usage for Llama models without Spring AI abstraction. " +
                     "This endpoint constructs the raw JSON payload according to Meta's Llama format and " +
                     "manually parses the response, showing the underlying API structure."
    )
    public ResponseEntity<ChatResponse> chatWithLlamaDirect(
            @Valid @RequestBody ChatRequest request) {
        
        logger.info("Direct Llama SDK request: model={}, message length={}", 
                   request.modelId(), request.message().length());

        // Validate that it's a Llama model
        if (!request.modelId().contains("llama")) {
            return ResponseEntity.badRequest().body(
                ChatResponse.error("This endpoint only supports Llama models. Use model ID containing 'llama'.", request.modelId())
            );
        }

        ChatResponse response = directBedrockService.invokeLlamaDirect(request);
        
        if (response.isSuccess()) {
            logger.info("Direct Llama SDK success: tokens={}, cost=${}", 
                       response.metrics().totalTokens(), response.metrics().estimatedCost());
            return ResponseEntity.ok(response);
        } else {
            logger.error("Direct Llama SDK error: {}", response.errorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/examples")
    @Operation(
        summary = "Get example requests for direct SDK calls",
        description = "Returns example request payloads for testing direct AWS Bedrock SDK calls"
    )
    public ResponseEntity<Map<String, Object>> getExamples() {
        Map<String, Object> examples = Map.of(
            "claude_example", Map.of(
                "message", "Explain the difference between AWS Bedrock SDK and Spring AI",
                "systemPrompt", "You are a helpful AI assistant explaining AWS services",
                "modelId", "claude-3-sonnet",
                "maxTokens", 1000,
                "temperature", 0.7,
                "topP", 0.9,
                "topK", 40,
                "stream", false,
                "includeMetrics", true
            ),
            "llama_example", Map.of(
                "message", "What are the benefits of using direct SDK calls?",
                "systemPrompt", "You are a technical expert explaining API architectures",
                "modelId", "llama2-70b",
                "maxTokens", 800,
                "temperature", 0.6,
                "topP", 0.85,
                "topK", 50,
                "stream", false,
                "includeMetrics", true
            ),
            "endpoints", Map.of(
                "claude_direct", "/api/direct-bedrock/claude",
                "llama_direct", "/api/direct-bedrock/llama"
            ),
            "notes", Map.of(
                "payload_construction", "These endpoints show how to construct model-specific JSON payloads",
                "response_parsing", "Demonstrates manual parsing of different model response formats",
                "cost_calculation", "Includes manual cost calculation based on token usage",
                "vs_spring_ai", "Compare with /api/chat endpoints to see Spring AI abstraction benefits"
            )
        );
        
        return ResponseEntity.ok(examples);
    }

    @GetMapping("/payload-formats")
    @Operation(
        summary = "Show raw payload formats for different models",
        description = "Displays the actual JSON payload structures used by different models in direct SDK calls"
    )
    public ResponseEntity<Map<String, Object>> getPayloadFormats() {
        Map<String, Object> formats = Map.of(
            "claude_payload_format", Map.of(
                "messages", "[{\"role\": \"user\", \"content\": \"Your message here\"}]",
                "max_tokens", "1000",
                "temperature", "0.7",
                "top_p", "0.9",
                "top_k", "40",
                "system", "Optional system prompt",
                "anthropic_version", "bedrock-2023-05-31"
            ),
            "claude_response_format", Map.of(
                "content", "[{\"text\": \"Response content\", \"type\": \"text\"}]",
                "usage", Map.of(
                    "input_tokens", "25",
                    "output_tokens", "150"
                ),
                "stop_reason", "end_turn",
                "model", "claude-3-sonnet"
            ),
            "llama_payload_format", Map.of(
                "prompt", "Human: Your message here\\n\\nAssistant:",
                "max_gen_len", "1000",
                "temperature", "0.7",
                "top_p", "0.9"
            ),
            "llama_response_format", Map.of(
                "generation", "Response content from Llama",
                "prompt_token_count", "25",
                "generation_token_count", "150",
                "stop_reason", "stop"
            )
        );
        
        return ResponseEntity.ok(formats);
    }
}