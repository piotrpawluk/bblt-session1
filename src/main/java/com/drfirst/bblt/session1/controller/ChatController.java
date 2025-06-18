package com.drfirst.bblt.session1.controller;

import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import com.drfirst.bblt.session1.service.BedrockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/chat")
@Validated
public class ChatController {

    private static final Logger log = Logger.getLogger(ChatController.class.getName());
    private final BedrockService bedrockService;

    public ChatController(BedrockService bedrockService) {
        this.bedrockService = bedrockService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AWS Bedrock Chat API",
                "timestamp", System.currentTimeMillis(),
                "availableModels", bedrockService.getAvailableModels()
        ));
    }

    @PostMapping("/completion")
    public ResponseEntity<ChatResponse> chatCompletion(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat completion request for model: " + request.modelId());
        
        try {
            ChatResponse response = bedrockService.processChat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.severe("Error processing chat completion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.error("Internal server error: " + e.getMessage(), request.modelId()));
        }
    }

    @PostMapping(value = "/completion/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatCompletionStream(@Valid @RequestBody ChatRequest request) {
        log.info("Received streaming chat completion request for model: " + request.modelId());
        
        return bedrockService.processStreamChat(request)
                .delayElements(Duration.ofMillis(50)) // Add small delay for better UX
                .doOnNext(content -> log.fine("Streaming chunk: " + content))
                .doOnError(error -> log.severe("Streaming error: " + error.getMessage()))
                .onErrorReturn("Error occurred during streaming");
    }

    @PostMapping("/completion/detailed")
    public ResponseEntity<ChatResponse> chatCompletionDetailed(@Valid @RequestBody ChatRequest request) {
        log.info("Received detailed chat completion request for model: " + request.modelId());
        
        // Force metrics to be included - create new request with includeMetrics = true
        ChatRequest detailedRequest = new ChatRequest(
            request.message(),
            request.systemPrompt(),
            request.modelId(),
            request.maxTokens(),
            request.temperature(),
            request.topP(),
            request.topK(),
            request.stream(),
            true // Force includeMetrics to true
        );
        
        try {
            ChatResponse response = bedrockService.processChat(detailedRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.severe("Error processing detailed chat completion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.error("Internal server error: " + e.getMessage(), request.modelId()));
        }
    }

    @PostMapping("/completion/system")
    public ResponseEntity<ChatResponse> chatWithSystemPrompt(
            @RequestParam String message,
            @RequestParam String systemPrompt,
            @RequestParam(defaultValue = "claude-3-sonnet") String modelId,
            @RequestParam(defaultValue = "0.7") Double temperature,
            @RequestParam(defaultValue = "1000") Integer maxTokens) {
        
        log.info("Received system prompt chat request for model: " + modelId);
        
        ChatRequest request = new ChatRequest(
            message,
            systemPrompt,
            modelId,
            maxTokens,
            temperature,
            0.9,
            40,
            false,
            true
        );
        
        try {
            ChatResponse response = bedrockService.processChat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.severe("Error processing system prompt chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.error("Internal server error: " + e.getMessage(), modelId));
        }
    }

    @GetMapping("/models/{modelId}/info")
    public ResponseEntity<Map<String, Object>> getModelInfo(@PathVariable String modelId) {
        log.info("Received model info request for: " + modelId);
        
        Map<String, Object> modelInfo = bedrockService.getModelInfo(modelId);
        
        if (modelInfo.containsKey("error")) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(modelInfo);
    }

    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getAvailableModels() {
        return ResponseEntity.ok(Map.of(
                "models", bedrockService.getAvailableModels(),
                "count", bedrockService.getAvailableModels().size(),
                "default", "claude-3-sonnet"
        ));
    }
}