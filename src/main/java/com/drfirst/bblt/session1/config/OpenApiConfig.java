package com.drfirst.bblt.session1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the AI Models Comparison API.
 * Provides comprehensive API documentation for AWS Bedrock, Google Vertex AI Gemini,
 * and direct SDK integrations.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Models Comparison API")
                        .version("1.0.0")
                        .description("""
                            # AI Models Comparison Demo
                            
                            This API demonstrates integration with multiple AI providers using Spring AI framework
                            and direct SDK approaches. It showcases the differences between various AI models and
                            integration patterns.
                            
                            ## Available Integrations
                            
                            ### AWS Bedrock (via Spring AI)
                            - **Claude 3 Sonnet** - Anthropic's powerful reasoning model
                            - **Claude 3.5 Sonnet** - Latest Anthropic model with enhanced capabilities  
                            - **Llama 2 70B** - Meta's open-source large language model
                            - **Amazon Titan Express** - Amazon's foundation model
                            
                            ### Google Vertex AI (via Spring AI)
                            - **Gemini 2.5 Flash** - Google's latest high-speed model with 2M context window
                            
                            ### Direct AWS SDK
                            - **Raw Bedrock SDK calls** - Direct integration without Spring AI abstraction
                            - **Manual JSON payload construction** - Shows underlying API structure
                            - **Custom response parsing** - Demonstrates model-specific response handling
                            
                            ## Key Features
                            
                            - **Model Comparison** - Side-by-side comparison of different AI models
                            - **Prompt Engineering** - Advanced prompting techniques and templates
                            - **Streaming Support** - Real-time response streaming
                            - **Cost Analysis** - Token usage and cost estimation
                            - **Performance Metrics** - Response time and throughput analysis
                            
                            ## Authentication Required
                            
                            - **AWS Credentials** - Set `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables
                            - **Google Cloud** - Configure `GOOGLE_VERTEX_PROJECT` and service account credentials
                            
                            ## Usage Tips
                            
                            1. Start with the `/api/health` endpoint to verify the service is running
                            2. Use `/api/models/list` to see available models and their configurations
                            3. Try the examples endpoints (`/api/*/examples`) for sample requests
                            4. Compare Spring AI vs Direct SDK approaches for learning purposes
                            """)
                        .contact(new Contact()
                                .name("DrFirst BBLT Session 1")
                                .email("support@drfirst.com")
                                .url("https://www.drfirst.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8911")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://your-domain.com")
                                .description("Production Server (if deployed)")
                ));
    }
}