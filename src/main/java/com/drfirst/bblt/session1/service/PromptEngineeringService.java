package com.drfirst.bblt.session1.service;

import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class PromptEngineeringService {

    private static final Logger log = Logger.getLogger(PromptEngineeringService.class.getName());
    private final BedrockService bedrockService;

    public PromptEngineeringService(BedrockService bedrockService) {
        this.bedrockService = bedrockService;
    }

    public ChatResponse processFewShotLearning(String userQuery, String domain, String modelId) {
        log.info("Processing few-shot learning for domain: " + domain + " with model: " + modelId);
        
        String systemPrompt = buildFewShotSystemPrompt(domain);
        String fewShotPrompt = buildFewShotPrompt(userQuery, domain);
        
        ChatRequest request = new ChatRequest(
            fewShotPrompt,
            systemPrompt,
            modelId,
            1500,
            0.3, // Lower temperature for more consistent responses
            0.9,
            40,
            false,
            true
        );
        
        return bedrockService.processChat(request);
    }

    public ChatResponse processChainOfThought(String problem, String modelId) {
        log.info("Processing chain of thought reasoning for problem with model: " + modelId);
        
        String systemPrompt = """
                You are an expert problem solver. When given a problem, break it down into clear, logical steps.
                Follow this format:
                1. Understanding: First, explain what the problem is asking
                2. Analysis: Break down the problem into smaller parts
                3. Step-by-step solution: Work through each step methodically
                4. Verification: Check your answer makes sense
                5. Final answer: Provide the clear, final answer
                
                Think step by step and show your reasoning clearly.
                """;
        
        String chainOfThoughtPrompt = String.format("""
                Problem: %s
                
                Please solve this problem using step-by-step reasoning. Show all your work and explain each step clearly.
                """, problem);
        
        ChatRequest request = new ChatRequest(
            chainOfThoughtPrompt,
            systemPrompt,
            modelId,
            2000,
            0.1, // Very low temperature for logical reasoning
            0.9,
            40,
            false,
            true
        );
        
        return bedrockService.processChat(request);
    }

    public ChatResponse processCodeReview(String code, String language, String modelId) {
        log.info("Processing code review for " + language + " code with model: " + modelId);
        
        String systemPrompt = """
                You are an experienced senior software engineer and code reviewer. 
                Your task is to review code for:
                1. Code quality and best practices
                2. Security vulnerabilities
                3. Performance optimizations
                4. Maintainability and readability
                5. Adherence to language-specific conventions
                
                Provide constructive feedback with specific examples and suggestions.
                """;
        
        String codeReviewPrompt = String.format("""
                Please review the following %s code:
                
                ```%s
                %s
                ```
                
                Provide a detailed code review including:
                - Overall assessment
                - Specific issues found
                - Recommendations for improvement
                - Security considerations
                - Performance suggestions
                """, language, language, code);
        
        ChatRequest request = new ChatRequest(
            codeReviewPrompt,
            systemPrompt,
            modelId,
            3000,
            0.2,
            0.9,
            40,
            false,
            true
        );
        
        return bedrockService.processChat(request);
    }

    public ChatResponse generateRecipe(String ingredients, String cuisine, String dietaryRestrictions, String modelId) {
        log.info("Generating recipe for " + cuisine + " cuisine with model: " + modelId);
        
        String systemPrompt = """
                You are a professional chef and recipe developer. Create delicious, practical recipes that are:
                - Easy to follow with clear instructions
                - Include precise measurements and cooking times
                - Consider dietary restrictions and preferences
                - Provide helpful cooking tips and variations
                """;
        
        String recipePrompt = String.format("""
                Create a %s recipe using the following ingredients: %s
                
                Dietary restrictions: %s
                
                Please provide:
                1. Recipe name
                2. Prep time and cook time
                3. Servings
                4. Complete ingredient list with measurements
                5. Step-by-step cooking instructions
                6. Chef's tips or variations
                7. Nutritional highlights
                """, cuisine, ingredients, dietaryRestrictions.isEmpty() ? "None" : dietaryRestrictions);
        
        ChatRequest request = new ChatRequest(
            recipePrompt,
            systemPrompt,
            modelId,
            2500,
            0.7, // Higher temperature for creativity
            0.9,
            40,
            false,
            true
        );
        
        return bedrockService.processChat(request);
    }

    public ChatResponse writeStory(String genre, String theme, String characters, String modelId) {
        log.info("Writing " + genre + " story with theme: " + theme + " using model: " + modelId);
        
        String systemPrompt = """
                You are a creative writer and storyteller. Write engaging stories that:
                - Have well-developed characters and plot
                - Include vivid descriptions and dialogue
                - Maintain consistent tone and style
                - Create emotional connection with readers
                - Follow proper story structure
                """;
        
        String storyPrompt = String.format("""
                Write a %s story with the following elements:
                Theme: %s
                Characters: %s
                
                Requirements:
                - Length: 800-1200 words
                - Include dialogue and descriptive scenes
                - Develop character relationships
                - Create a satisfying conclusion
                - Maintain appropriate tone for the genre
                """, genre, theme, characters);
        
        ChatRequest request = new ChatRequest(
            storyPrompt,
            systemPrompt,
            modelId,
            4000,
            0.8, // High temperature for creativity
            0.9,
            40,
            false,
            true
        );
        
        return bedrockService.processChat(request);
    }

    private String buildFewShotSystemPrompt(String domain) {
        return switch (domain.toLowerCase()) {
            case "sentiment" -> """
                    You are a sentiment analysis expert. Classify text as POSITIVE, NEGATIVE, or NEUTRAL.
                    Consider context, tone, and emotional indicators.
                    """;
            case "classification" -> """
                    You are a text classification expert. Categorize the input based on the examples provided.
                    Use the same categories and format as shown in the examples.
                    """;
            case "translation" -> """
                    You are a professional translator. Provide accurate, natural-sounding translations
                    that preserve meaning, tone, and cultural context.
                    """;
            default -> """
                    You are an expert assistant. Follow the patterns shown in the examples
                    to provide accurate and helpful responses.
                    """;
        };
    }

    private String buildFewShotPrompt(String userQuery, String domain) {
        String examples = getFewShotExamples(domain);
        return String.format("""
                %s
                
                Now apply the same pattern to this:
                %s
                """, examples, userQuery);
    }

    private String getFewShotExamples(String domain) {
        return switch (domain.toLowerCase()) {
            case "sentiment" -> """
                    Examples:
                    
                    Text: "I love this product! It works perfectly and exceeded my expectations."
                    Sentiment: POSITIVE
                    
                    Text: "This item broke after one day. Completely disappointed and frustrated."
                    Sentiment: NEGATIVE
                    
                    Text: "The product is okay. It does what it's supposed to do."
                    Sentiment: NEUTRAL
                    """;
            case "classification" -> """
                    Examples:
                    
                    Text: "How do I reset my password?"
                    Category: TECHNICAL_SUPPORT
                    
                    Text: "I want to return this item for a refund"
                    Category: RETURNS
                    
                    Text: "When will my order arrive?"
                    Category: SHIPPING
                    """;
            case "translation" -> """
                    Examples:
                    
                    English: "Good morning, how are you today?"
                    Spanish: "Buenos días, ¿cómo estás hoy?"
                    
                    English: "Thank you for your help"
                    Spanish: "Gracias por tu ayuda"
                    
                    English: "Where is the nearest restaurant?"
                    Spanish: "¿Dónde está el restaurante más cercano?"
                    """;
            default -> """
                    Examples will be provided based on the specific task.
                    """;
        };
    }

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