package com.drfirst.bblt.session1.controller;

import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import com.drfirst.bblt.session1.service.BedrockService;
import com.drfirst.bblt.session1.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/models")
public class ModelComparisonController {

    private static final Logger log = Logger.getLogger(ModelComparisonController.class.getName());
    private final BedrockService bedrockService;
    private final GeminiService geminiService;

    public ModelComparisonController(BedrockService bedrockService, GeminiService geminiService) {
        this.bedrockService = bedrockService;
        this.geminiService = geminiService;
    }

    @PostMapping("/compare")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> compareModels(
            @RequestParam String message,
            @RequestParam(defaultValue = "claude-3-7-sonnet,claude-4-opus,nova-pro,gemini-2.5-flash") List<String> modelIds) {
        
        log.info("Comparing models: " + modelIds + " with message: " + message);
        
        return compareAllModels(message, modelIds)
                .thenApply(metrics -> {
                    // Sort by response time for easy comparison
                    List<ChatResponse.ModelPerformanceMetrics> sortedMetrics = metrics.stream()
                            .sorted((a, b) -> Long.compare(a.responseTimeMs(), b.responseTimeMs()))
                            .toList();
                    
                    // Calculate comparison statistics
                    Map<String, Object> comparison = Map.of(
                            "prompt", message,
                            "modelCount", modelIds.size(),
                            "results", sortedMetrics,
                            "summary", generateComparisonSummary(sortedMetrics)
                    );
                    
                    return ResponseEntity.ok(comparison);
                })
                .exceptionally(throwable -> {
                    log.severe("Error comparing models: " + throwable.getMessage());
                    return ResponseEntity.internalServerError()
                            .body(Map.of("error", "Failed to compare models: " + throwable.getMessage()));
                });
    }

    @PostMapping("/benchmark")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> benchmarkModels(
            @RequestParam(defaultValue = "claude-3-7-sonnet,claude-4-opus,nova-pro,gemini-2.5-flash") List<String> modelIds) {
        
        log.info("Benchmarking models: " + modelIds);
        
        List<String> benchmarkPrompts = List.of(
                "Write a haiku about artificial intelligence",
                "Explain quantum computing in simple terms",
                "Write a Python function to calculate Fibonacci numbers",
                "Summarize the benefits of renewable energy in 100 words",
                "Create a short story about a robot learning to paint"
        );
        
        List<CompletableFuture<List<ChatResponse.ModelPerformanceMetrics>>> futures = benchmarkPrompts.stream()
                .map(prompt -> compareAllModels(prompt, modelIds))
                .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<List<ChatResponse.ModelPerformanceMetrics>> allResults = futures.stream()
                            .map(CompletableFuture::join)
                            .toList();
                    
                    Map<String, Object> benchmark = generateBenchmarkReport(allResults, benchmarkPrompts);
                    return ResponseEntity.ok(benchmark);
                })
                .exceptionally(throwable -> {
                    log.severe("Error benchmarking models: " + throwable.getMessage());
                    return ResponseEntity.internalServerError()
                            .body(Map.of("error", "Failed to benchmark models: " + throwable.getMessage()));
                });
    }

    @GetMapping("/performance/summary")
    public ResponseEntity<Map<String, Object>> getPerformanceSummary() {
        List<String> availableModels = bedrockService.getAvailableModels();
        
        Map<String, Object> summary = Map.of(
                "availableModels", availableModels.size(),
                "models", availableModels.stream()
                        .map(modelId -> bedrockService.getModelInfo(modelId))
                        .toList(),
                "comparisonMetrics", List.of(
                        "Response Time (ms)",
                        "Token Count (input/output)",
                        "Cost Estimation ($)",
                        "Tokens per Second",
                        "Quality Assessment"
                ),
                "recommendedUses", Map.of(
                        "claude-3-7-sonnet", "Enhanced reasoning, improved accuracy",
                        "claude-4-opus", "Most capable model, advanced reasoning",
                        "nova-pro", "AWS native, high context window",
                        "titan-express", "Fast responses, AWS native",
                        "gemini-2.5-flash", "Google AI, ultra-fast, 2M token context"
                )
        );
        
        return ResponseEntity.ok(summary);
    }

    private CompletableFuture<List<ChatResponse.ModelPerformanceMetrics>> compareAllModels(String message, List<String> modelIds) {
        log.info("Comparing all models (Bedrock + Gemini): " + modelIds + " with message: " + message);

        List<CompletableFuture<ChatResponse.ModelPerformanceMetrics>> futures = modelIds.stream()
                .map(modelId -> CompletableFuture.supplyAsync(() -> {
                    ChatRequest request = new ChatRequest(
                            message, null, modelId, 1000, 0.7, 0.9, 40, false, true
                    );

                    ChatResponse response;
                    if (modelId.startsWith("gemini")) {
                        // Use GeminiService for Gemini models
                        response = geminiService.chatCompletion(request);
                    } else {
                        // Use BedrockService for AWS Bedrock models
                        response = bedrockService.processChat(request);
                    }

                    if (response.metrics() != null) {
                        return response.metrics();
                    } else {
                        // Create default metrics if none provided
                        return ChatResponse.ModelPerformanceMetrics.create(
                                modelId, 0, 0, 0, 0.0, "unknown"
                        );
                    }
                }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    private Map<String, Object> generateComparisonSummary(List<ChatResponse.ModelPerformanceMetrics> metrics) {
        if (metrics.isEmpty()) {
            return Map.of("error", "No metrics available");
        }
        
        ChatResponse.ModelPerformanceMetrics fastest = metrics.get(0);
        ChatResponse.ModelPerformanceMetrics mostTokens = metrics.stream()
                .max((a, b) -> Integer.compare(a.outputTokens(), b.outputTokens()))
                .orElse(fastest);
        ChatResponse.ModelPerformanceMetrics mostCostEffective = metrics.stream()
                .min((a, b) -> Double.compare(a.estimatedCost(), b.estimatedCost()))
                .orElse(fastest);
        
        return Map.of(
                "fastest", Map.of(
                        "model", "Model",
                        "responseTime", fastest.responseTimeMs() + "ms"
                ),
                "mostVerbose", Map.of(
                        "model", "Model",
                        "outputTokens", mostTokens.outputTokens()
                ),
                "mostCostEffective", Map.of(
                        "model", "Model",
                        "estimatedCost", String.format("$%.6f", mostCostEffective.estimatedCost())
                ),
                "averageResponseTime", metrics.stream()
                        .mapToLong(ChatResponse.ModelPerformanceMetrics::responseTimeMs)
                        .average()
                        .orElse(0.0),
                "totalEstimatedCost", metrics.stream()
                        .mapToDouble(ChatResponse.ModelPerformanceMetrics::estimatedCost)
                        .sum()
        );
    }

    private Map<String, Object> generateBenchmarkReport(List<List<ChatResponse.ModelPerformanceMetrics>> allResults, 
                                                       List<String> prompts) {
        Map<String, Double> avgResponseTimes = calculateAverageResponseTimes(allResults);
        Map<String, Double> avgCosts = calculateAverageCosts(allResults);
        Map<String, Integer> avgTokens = calculateAverageTokens(allResults);
        
        return Map.of(
                "benchmarkPrompts", prompts,
                "totalTests", prompts.size(),
                "averageResponseTimes", avgResponseTimes,
                "averageCosts", avgCosts,
                "averageOutputTokens", avgTokens,
                "detailedResults", allResults,
                "recommendations", generateRecommendations(avgResponseTimes, avgCosts, avgTokens)
        );
    }

    private Map<String, Double> calculateAverageResponseTimes(List<List<ChatResponse.ModelPerformanceMetrics>> allResults) {
        return allResults.stream()
                .flatMap(List::stream)
                .collect(java.util.stream.Collectors.groupingBy(
                        ChatResponse.ModelPerformanceMetrics::modelId,
                        java.util.stream.Collectors.averagingLong(ChatResponse.ModelPerformanceMetrics::responseTimeMs)
                ));
    }

    private Map<String, Double> calculateAverageCosts(List<List<ChatResponse.ModelPerformanceMetrics>> allResults) {
        return allResults.stream()
                .flatMap(List::stream)
                .collect(java.util.stream.Collectors.groupingBy(
                        ChatResponse.ModelPerformanceMetrics::modelId,
                        java.util.stream.Collectors.averagingDouble(ChatResponse.ModelPerformanceMetrics::estimatedCost)
                ));
    }

    private Map<String, Integer> calculateAverageTokens(List<List<ChatResponse.ModelPerformanceMetrics>> allResults) {
        return allResults.stream()
                .flatMap(List::stream)
                .collect(java.util.stream.Collectors.groupingBy(
                        ChatResponse.ModelPerformanceMetrics::modelId,
                        java.util.stream.Collectors.averagingInt(ChatResponse.ModelPerformanceMetrics::outputTokens)
                )).entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().intValue()
                ));
    }

    private Map<String, String> generateRecommendations(Map<String, Double> avgResponseTimes,
                                                       Map<String, Double> avgCosts,
                                                       Map<String, Integer> avgTokens) {
        String fastestModel = avgResponseTimes.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
        
        String cheapestModel = avgCosts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
        
        String mostVerboseModel = avgTokens.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
        
        return Map.of(
                "speed", "For fastest responses, use: " + fastestModel,
                "cost", "For most cost-effective option, use: " + cheapestModel,
                "detail", "For most detailed responses, use: " + mostVerboseModel,
                "general", "For balanced performance, Claude 3.7 Sonnet is recommended"
        );
    }
}