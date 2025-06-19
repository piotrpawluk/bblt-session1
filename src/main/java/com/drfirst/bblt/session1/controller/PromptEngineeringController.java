package com.drfirst.bblt.session1.controller;

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


    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getExamples() {
        return ResponseEntity.ok(Map.of(
                "note", "All prompt templates are now implemented on the frontend for transparency",
                "usage", "Click the Test buttons on the main page to see full prompts in the chat input before sending",
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

}