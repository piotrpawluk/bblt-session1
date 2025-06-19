package com.drfirst.bblt.session1.controller;

import com.drfirst.bblt.session1.model.*;
import com.drfirst.bblt.session1.service.PromptEngineeringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/prompt-engineering")
public class PromptEngineeringController {

    private static final Logger log = Logger.getLogger(PromptEngineeringController.class.getName());
    private final PromptEngineeringService promptEngineeringService;

    public PromptEngineeringController(PromptEngineeringService promptEngineeringService) {
        this.promptEngineeringService = promptEngineeringService;
    }

    @GetMapping("/techniques")
    public ResponseEntity<Map<String, Object>> getPromptingTechniques() {
        return ResponseEntity.ok(promptEngineeringService.getPromptingTechniques());
    }

    @PostMapping("/few-shot")
    public ResponseEntity<ChatResponse> fewShotLearning(@RequestBody FewShotRequest request) {
        
        log.info("Processing few-shot learning request for domain: " + request.getDomain() + " with model: " + request.getModelId());
        
        ChatResponse response = promptEngineeringService.processFewShotLearning(request.getQuery(), request.getDomain(), request.getModelId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chain-of-thought")
    public ResponseEntity<ChatResponse> chainOfThought(@RequestBody ChainOfThoughtRequest request) {
        
        log.info("Processing chain of thought request with model: " + request.getModelId());
        
        ChatResponse response = promptEngineeringService.processChainOfThought(request.getProblem(), request.getModelId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/code-review")
    public ResponseEntity<ChatResponse> codeReview(@RequestBody CodeReviewRequest request) {
        
        log.info("Processing code review request for " + request.getLanguage() + " with model: " + request.getModelId());
        
        ChatResponse response = promptEngineeringService.processCodeReview(request.getCode(), request.getLanguage(), request.getModelId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recipe-generator")
    public ResponseEntity<ChatResponse> generateRecipe(@RequestBody RecipeRequest request) {
        
        log.info("Generating " + request.getCuisine() + " recipe with model: " + request.getModelId());
        
        ChatResponse response = promptEngineeringService.generateRecipe(request.getIngredients(), request.getCuisine(), request.getDietaryRestrictions(), request.getModelId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/story-writer")
    public ResponseEntity<ChatResponse> writeStory(@RequestBody StoryRequest request) {
        
        log.info("Writing " + request.getGenre() + " story with theme: " + request.getTheme() + " using model: " + request.getModelId());
        
        ChatResponse response = promptEngineeringService.writeStory(request.getGenre(), request.getTheme(), request.getCharacters(), request.getModelId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getExamples() {
        return ResponseEntity.ok(Map.of(
                "fewShotDomains", Map.of(
                        "sentiment", "Classify text sentiment as POSITIVE, NEGATIVE, or NEUTRAL",
                        "classification", "Categorize customer support tickets",
                        "translation", "Translate text between languages"
                ),
                "chainOfThoughtExamples", Map.of(
                        "math", "Solve: If a train travels 120 miles in 2 hours, what is its average speed?",
                        "logic", "If all roses are flowers and some flowers are red, can we conclude that some roses are red?",
                        "programming", "How would you implement a binary search algorithm?"
                ),
                "codeReviewLanguages", Map.of(
                        "java", "Java code review focusing on Spring Boot best practices",
                        "python", "Python code review with PEP8 compliance",
                        "javascript", "JavaScript code review for Node.js applications",
                        "typescript", "TypeScript code review with type safety focus"
                ),
                "recipeStyles", Map.of(
                        "Italian", "Traditional Italian cuisine",
                        "Asian", "Various Asian cooking styles",
                        "Mediterranean", "Healthy Mediterranean diet",
                        "Mexican", "Authentic Mexican flavors"
                ),
                "storyGenres", Map.of(
                        "sci-fi", "Science fiction and futuristic themes",
                        "fantasy", "Magical and mythical storytelling",
                        "mystery", "Detective and crime solving stories",
                        "romance", "Love and relationship narratives"
                )
        ));
    }

    @PostMapping("/temperature-test")
    public ResponseEntity<Map<String, ChatResponse>> temperatureTest(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "claude-3-sonnet") String modelId) {
        
        log.info("Running temperature test with model: " + modelId);
        
        // Test different temperatures to show their effects
        double[] temperatures = {0.1, 0.5, 0.9};
        Map<String, ChatResponse> results = new java.util.HashMap<>();
        
        for (double temp : temperatures) {
            ChatRequest request = new ChatRequest(
                prompt,
                null,
                modelId,
                500,
                temp,
                0.9,
                40,
                false,
                true
            );
            
            // Need to access BedrockService through PromptEngineeringService
            // This should be implemented in the service layer
            results.put("temperature_" + temp, null); // TODO: Implement temperature testing
        }
        
        return ResponseEntity.ok(results);
    }

    @PostMapping("/prompt-optimization")
    public ResponseEntity<Map<String, Object>> optimizePrompt(
            @RequestParam String originalPrompt,
            @RequestParam String goal,
            @RequestParam(defaultValue = "claude-3-sonnet") String modelId) {
        
        log.info("Optimizing prompt for goal: " + goal + " with model: " + modelId);
        
        String systemPrompt = """
                You are a prompt engineering expert. Your task is to optimize prompts for better AI responses.
                
                Analyze the given prompt and goal, then provide:
                1. Issues with the original prompt
                2. Optimized version with clear instructions
                3. Explanation of improvements made
                4. Best practices applied
                """;
        
        String optimizationPrompt = String.format("""
                Original prompt: "%s"
                Goal: %s
                
                Please provide an optimized version of this prompt that will achieve the goal more effectively.
                Include specific techniques like role assignment, clear instructions, examples if needed, and output format specification.
                """, originalPrompt, goal);
        
        ChatRequest request = new ChatRequest(
            optimizationPrompt,
            systemPrompt,
            modelId,
            2000,
            0.3,
            0.9,
            40,
            false,
            true
        );
        
        // TODO: Implement optimization logic in service layer
        ChatResponse response = null;
        
        return ResponseEntity.ok(Map.of(
                "originalPrompt", originalPrompt,
                "goal", goal,
                "optimization", response,
                "promptEngineeringTips", Map.of(
                        "clarity", "Be specific and clear about what you want",
                        "context", "Provide relevant background information",
                        "examples", "Include examples for complex tasks",
                        "format", "Specify the desired output format",
                        "role", "Assign a specific role or expertise to the AI"
                )
        ));
    }
}