package com.drfirst.bblt.session1.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Simplified service providing documentation and examples for prompt engineering techniques.
 * All prompt construction has been moved to the frontend for transparency and user control.
 */
@Service
public class PromptEngineeringService {

    private static final Logger log = Logger.getLogger(PromptEngineeringService.class.getName());

    public Map<String, Object> getPromptingTechniques() {
        return Map.of(
                "few-shot", Map.of(
                        "description", "Learn from examples to perform similar tasks",
                        "bestFor", List.of("Classification", "Pattern recognition", "Style mimicking"),
                        "temperature", "0.1-0.3 (low for consistency)"
                ),
                "chain-of-thought", Map.of(
                        "description", "Break down complex problems into logical steps",
                        "bestFor", List.of("Math problems", "Logical reasoning", "Complex analysis"),
                        "temperature", "0.1-0.2 (very low for logical thinking)"
                ),
                "zero-shot", Map.of(
                        "description", "Perform tasks without examples using general knowledge",
                        "bestFor", List.of("General questions", "Simple tasks", "Creative writing"),
                        "temperature", "0.7-0.9 (higher for creativity)"
                ),
                "role-playing", Map.of(
                        "description", "Adopt a specific persona or expertise role",
                        "bestFor", List.of("Domain expertise", "Consistent style", "Professional advice"),
                        "temperature", "0.3-0.7 (moderate)"
                )
        );
    }
}