package com.drfirst.bblt.session1.service;

import com.drfirst.bblt.session1.config.ModelConfig;
import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockruntime.model.AccessDeniedException;
import software.amazon.awssdk.services.bedrockruntime.model.ThrottlingException;
import software.amazon.awssdk.services.bedrockruntime.model.ValidationException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced error handler with retry logic, exponential backoff, and model fallback for AWS Bedrock calls.
 * Implements comprehensive resilience patterns for production-grade LLM applications.
 */
@Component
public class BedrockErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(BedrockErrorHandler.class);
    
    private final ModelConfig modelConfig;
    private ModelInvoker modelInvoker; // Will be set after construction to avoid circular dependency
    
    // Circuit breaker state tracking
    private final Map<String, CircuitBreakerState> circuitBreakerStates = new ConcurrentHashMap<>();
    private final Map<String, Long> lastFailureTime = new ConcurrentHashMap<>();
    private final Map<String, Integer> consecutiveFailures = new ConcurrentHashMap<>();
    
    // Fallback model hierarchy: Claude -> Nova -> Titan
    private final List<String> fallbackOrder = List.of(
        "claude-3-7-sonnet",
        "nova-pro",
        "titan-express"
    );
    
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final Duration CIRCUIT_BREAKER_TIMEOUT = Duration.ofMinutes(5);

    public BedrockErrorHandler(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }

    /**
     * Set the model invoker to break circular dependency
     */
    public void setModelInvoker(ModelInvoker modelInvoker) {
        this.modelInvoker = modelInvoker;
    }

    /**
     * Primary method with retry logic for throttling and transient errors
     * This will be called by the actual service implementations
     */
    @Retryable(
        retryFor = {ThrottlingException.class, ValidationException.class, RuntimeException.class},
        maxAttempts = 4,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
    )
    public ChatResponse invokeWithRetry(ChatRequest request) {
        String modelId = request.modelId();
        
        logger.info("Attempting to invoke model: {} with retry protection", modelId);
        
        // Check circuit breaker state
        if (isCircuitBreakerOpen(modelId)) {
            logger.warn("Circuit breaker is OPEN for model: {}, attempting fallback", modelId);
            return attemptFallback(request, "Circuit breaker is open for model: " + modelId);
        }
        
        try {
            // This will be overridden by actual implementation - for now throw to trigger retry/fallback
            recordFailure(modelId);
            throw new RuntimeException("This method should be overridden by the calling service");
            
        } catch (ThrottlingException e) {
            recordFailure(modelId);
            logger.warn("Throttling detected for model: {}, attempt will be retried", modelId);
            throw e; // Will trigger retry
            
        } catch (AccessDeniedException e) {
            recordFailure(modelId);
            logger.error("Access denied for model: {}, attempting fallback", modelId);
            return attemptFallback(request, "Access denied for model: " + modelId);
            
        } catch (ValidationException e) {
            recordFailure(modelId);
            logger.warn("Validation error for model: {}, will retry: {}", modelId, e.getMessage());
            throw e; // Will trigger retry
            
        } catch (Exception e) {
            recordFailure(modelId);
            logger.error("Unexpected error with model: {}, attempting fallback: {}", modelId, e.getMessage());
            return attemptFallback(request, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Recovery method when all retries are exhausted
     */
    @Recover
    public ChatResponse recover(Exception ex, ChatRequest request) {
        logger.error("All retry attempts exhausted for model: {}, attempting final fallback", 
                    request.modelId());
        return attemptFallback(request, "All retries exhausted: " + ex.getMessage());
    }

    /**
     * Attempt fallback to alternative models - REAL IMPLEMENTATION
     */
    private ChatResponse attemptFallback(ChatRequest request, String originalError) {
        String originalModelId = request.modelId();
        
        if (modelInvoker == null) {
            logger.error("ModelInvoker not set - cannot perform fallback");
            return ChatResponse.error(
                "⚠️ Model " + originalModelId + " is currently unavailable. " +
                "Fallback system not initialized. Original error: " + originalError,
                originalModelId
            );
        }
        
        logger.info("Starting fallback sequence for failed model: {}", originalModelId);
        
        // Try each fallback model in order
        for (String fallbackModelId : fallbackOrder) {
            if (fallbackModelId.equals(originalModelId)) {
                logger.debug("Skipping original failing model: {}", fallbackModelId);
                continue; // Skip the original failing model
            }
            
            if (isCircuitBreakerOpen(fallbackModelId)) {
                logger.warn("Skipping fallback model {} - circuit breaker is open", fallbackModelId);
                continue;
            }
            
            try {
                logger.info("Attempting fallback from {} to {}", originalModelId, fallbackModelId);
                long fallbackStartTime = System.currentTimeMillis();
                
                // Create new request with fallback model
                ChatRequest fallbackRequest = new ChatRequest(
                    request.message(),
                    request.systemPrompt(),
                    fallbackModelId,
                    request.maxTokens(),
                    request.temperature(),
                    request.topP(),
                    request.topK(),
                    request.stream(),
                    request.includeMetrics()
                );
                
                // Invoke the fallback model directly (no retry logic to avoid infinite loops)
                ChatResponse fallbackResponse = modelInvoker.invokeModelDirect(fallbackRequest);
                
                if (fallbackResponse.isSuccess()) {
                    recordSuccess(fallbackModelId);
                    
                    long fallbackTime = System.currentTimeMillis() - fallbackStartTime;
                    logger.info("Successfully failed over from {} to {} in {}ms", 
                               originalModelId, fallbackModelId, fallbackTime);
                    
                    // Create new response with fallback notification
                    String fallbackMessage = String.format(
                        "⚠️ Fallback used: %s → %s (took %dms)\n" +
                        "Original error: %s\n\n%s",
                        originalModelId, 
                        fallbackModelId,
                        fallbackTime,
                        originalError,
                        fallbackResponse.content()
                    );
                    
                    // Update metrics to show fallback was used
                    ChatResponse.ModelPerformanceMetrics updatedMetrics = null;
                    if (fallbackResponse.metrics() != null) {
                        updatedMetrics = ChatResponse.ModelPerformanceMetrics.create(
                            fallbackModelId,
                            fallbackResponse.metrics().responseTimeMs() + fallbackTime,
                            fallbackResponse.metrics().inputTokens(),
                            fallbackResponse.metrics().outputTokens(),
                            fallbackResponse.metrics().estimatedCost(),
                            "FALLBACK_SUCCESS"
                        );
                    }
                    
                    return ChatResponse.success(fallbackMessage, fallbackModelId, updatedMetrics);
                    
                } else {
                    logger.warn("Fallback model {} returned error: {}", fallbackModelId, fallbackResponse.errorMessage());
                    recordFailure(fallbackModelId);
                }
                
            } catch (Exception e) {
                logger.warn("Fallback model {} threw exception: {}", fallbackModelId, e.getMessage());
                recordFailure(fallbackModelId);
            }
        }
        
        // All fallbacks failed
        logger.error("All fallback models failed for original request to {}", originalModelId);
        
        // Return detailed error with attempted fallbacks
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("⚠️ All models currently unavailable\n\n");
        errorMessage.append("Original model: ").append(originalModelId).append("\n");
        errorMessage.append("Original error: ").append(originalError).append("\n");
        errorMessage.append("Attempted fallbacks: ").append(String.join(", ", fallbackOrder)).append("\n");
        errorMessage.append("Please try again later or contact support.\n\n");
        errorMessage.append("Circuit breaker status:\n");
        
        // Add circuit breaker status for diagnostics
        getCircuitBreakerStatus().forEach((model, status) -> {
            errorMessage.append("- ").append(model).append(": ").append(status).append("\n");
        });
        
        return ChatResponse.error(errorMessage.toString(), originalModelId);
    }


    /**
     * Circuit breaker logic
     */
    private boolean isCircuitBreakerOpen(String modelId) {
        CircuitBreakerState state = circuitBreakerStates.get(modelId);
        if (state == CircuitBreakerState.CLOSED) {
            return false;
        }
        
        if (state == CircuitBreakerState.OPEN) {
            Long lastFailure = lastFailureTime.get(modelId);
            if (lastFailure != null && 
                System.currentTimeMillis() - lastFailure > CIRCUIT_BREAKER_TIMEOUT.toMillis()) {
                // Move to half-open state
                circuitBreakerStates.put(modelId, CircuitBreakerState.HALF_OPEN);
                logger.info("Circuit breaker for {} moved to HALF_OPEN state", modelId);
                return false;
            }
            return true;
        }
        
        // HALF_OPEN state - allow one request through
        return false;
    }

    public void recordSuccess(String modelId) {
        consecutiveFailures.put(modelId, 0);
        circuitBreakerStates.put(modelId, CircuitBreakerState.CLOSED);
        logger.debug("Recorded success for model: {}", modelId);
    }

    public void recordFailure(String modelId) {
        int failures = consecutiveFailures.getOrDefault(modelId, 0) + 1;
        consecutiveFailures.put(modelId, failures);
        lastFailureTime.put(modelId, System.currentTimeMillis());
        
        if (failures >= CIRCUIT_BREAKER_THRESHOLD) {
            circuitBreakerStates.put(modelId, CircuitBreakerState.OPEN);
            logger.warn("Circuit breaker OPENED for model: {} after {} consecutive failures", 
                       modelId, failures);
        }
        
        logger.debug("Recorded failure #{} for model: {}", failures, modelId);
    }

    /**
     * Rough token estimation (improve with actual tokenizer if needed)
     */
    private int estimateTokens(String text) {
        return Math.max(1, text.length() / 4);
    }

    /**
     * Circuit breaker states
     */
    private enum CircuitBreakerState {
        CLOSED,    // Normal operation
        OPEN,      // Failing fast
        HALF_OPEN  // Testing if service recovered
    }

    /**
     * Get circuit breaker status for monitoring
     */
    public Map<String, String> getCircuitBreakerStatus() {
        Map<String, String> status = new ConcurrentHashMap<>();
        circuitBreakerStates.forEach((modelId, state) -> {
            int failures = consecutiveFailures.getOrDefault(modelId, 0);
            status.put(modelId, state + " (failures: " + failures + ")");
        });
        return status;
    }

}