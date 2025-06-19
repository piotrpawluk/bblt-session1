package com.drfirst.bblt.session1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class BedrockDemoApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8911");
        SpringApplication.run(BedrockDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner startup() {
        return args -> {
            System.out.println("==============================================");
            System.out.println("AWS Bedrock Spring AI Demo Application Started");
            System.out.println("==============================================");
            System.out.println("Week 1 Presentation - Foundation Models & API Integration");
            System.out.println("");
            System.out.println("Available endpoints:");
            System.out.println("  - GET  /api/health");
            System.out.println("  - POST /api/chat/completion");
            System.out.println("  - POST /api/chat/stream");
            System.out.println("  - POST /api/models/compare");
            System.out.println("  - GET  /api/models/list");
            System.out.println("  - POST /api/prompt-engineering/few-shot");
            System.out.println("  - POST /api/prompt-engineering/chain-of-thought");
            System.out.println("");
            System.out.println("Direct AWS Bedrock SDK (without Spring AI):");
            System.out.println("  - POST /api/direct-bedrock/claude");
            System.out.println("  - POST /api/direct-bedrock/nova-pro");
            System.out.println("  - GET  /api/direct-bedrock/examples");
            System.out.println("  - GET  /api/direct-bedrock/payload-formats");
            System.out.println("");
            System.out.println("Google Vertex AI Gemini (with Spring AI):");
            System.out.println("  - POST /api/gemini/chat");
            System.out.println("  - POST /api/gemini/stream");
            System.out.println("  - POST /api/gemini/compare-bedrock");
            System.out.println("  - GET  /api/gemini/info");
            System.out.println("  - GET  /api/gemini/examples");
            System.out.println("");
            System.out.println("API Documentation:");
            System.out.println("  - GET  /swagger-ui.html (Swagger UI)");
            System.out.println("  - GET  /api-docs (OpenAPI JSON)");
            System.out.println("");
            System.out.println("Make sure AWS credentials are configured!");
            System.out.println("==============================================");
        };
    }
}