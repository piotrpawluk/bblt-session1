package com.drfirst.bblt.session1.controller;

import com.drfirst.bblt.session1.service.BedrockErrorHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test controller for simulating failures and testing circuit breaker behavior
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final BedrockErrorHandler errorHandler;

    public TestController(BedrockErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @PostMapping("/simulate-failures/{modelId}/{count}")
    public ResponseEntity<Map<String, Object>> simulateFailures(
            @PathVariable String modelId, 
            @PathVariable int count) {
        
        for (int i = 0; i < count; i++) {
            errorHandler.recordFailure(modelId);
        }
        
        return ResponseEntity.ok(Map.of(
                "message", "Simulated " + count + " failures for model " + modelId,
                "circuitBreakerStatus", errorHandler.getCircuitBreakerStatus()
        ));
    }

    @PostMapping("/simulate-success/{modelId}")
    public ResponseEntity<Map<String, Object>> simulateSuccess(@PathVariable String modelId) {
        errorHandler.recordSuccess(modelId);
        
        return ResponseEntity.ok(Map.of(
                "message", "Simulated success for model " + modelId,
                "circuitBreakerStatus", errorHandler.getCircuitBreakerStatus()
        ));
    }

    @PostMapping("/reset-circuit-breaker/{modelId}")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker(@PathVariable String modelId) {
        errorHandler.recordSuccess(modelId);
        
        return ResponseEntity.ok(Map.of(
                "message", "Reset circuit breaker for model " + modelId,
                "circuitBreakerStatus", errorHandler.getCircuitBreakerStatus()
        ));
    }
}