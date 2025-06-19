package com.drfirst.bblt.session1.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "bedrock.models")
public class ModelConfig {

    private Map<String, ModelProperties> models = new HashMap<>();

    public Map<String, ModelProperties> getModels() {
        return models;
    }

    public void setModels(Map<String, ModelProperties> models) {
        this.models = models;
    }

    @PostConstruct
    public void init() {
        // Default Claude 3 Sonnet configuration
        ModelProperties claudeSonnet = new ModelProperties();
        claudeSonnet.setModelId("anthropic.claude-3-sonnet-20240229-v1:0");
        claudeSonnet.setDisplayName("Claude 3 Sonnet");
        claudeSonnet.setProvider("Anthropic");
        claudeSonnet.setMaxTokens(4096);
        claudeSonnet.setContextWindow(200000);
        claudeSonnet.setCostPer1kInputTokens(0.003);
        claudeSonnet.setCostPer1kOutputTokens(0.015);
        models.put("claude-3-sonnet", claudeSonnet);

        // Claude 3.5 Sonnet configuration (original version)
        ModelProperties claude35Sonnet = new ModelProperties();
        claude35Sonnet.setModelId("anthropic.claude-3-5-sonnet-20240620-v1:0");
        claude35Sonnet.setDisplayName("Claude 3.5 Sonnet");
        claude35Sonnet.setProvider("Anthropic");
        claude35Sonnet.setMaxTokens(4096);
        claude35Sonnet.setContextWindow(200000);
        claude35Sonnet.setCostPer1kInputTokens(0.003);
        claude35Sonnet.setCostPer1kOutputTokens(0.015);
        models.put("claude-3-5-sonnet", claude35Sonnet);


        // Amazon Titan configuration
        ModelProperties titan = new ModelProperties();
        titan.setModelId("amazon.titan-text-express-v1");
        titan.setDisplayName("Amazon Titan Text Express");
        titan.setProvider("Amazon");
        titan.setMaxTokens(8192);
        titan.setContextWindow(8192);
        titan.setCostPer1kInputTokens(0.0002);
        titan.setCostPer1kOutputTokens(0.0006);
        models.put("titan-express", titan);

        // Amazon Nova Pro configuration
        ModelProperties novaPro = new ModelProperties();
        novaPro.setModelId("amazon.nova-pro-v1:0");
        novaPro.setDisplayName("Amazon Nova Pro");
        novaPro.setProvider("Amazon");
        novaPro.setMaxTokens(5120);
        novaPro.setContextWindow(300000);
        novaPro.setCostPer1kInputTokens(0.0008);
        novaPro.setCostPer1kOutputTokens(0.0032);
        models.put("nova-pro", novaPro);
    }

    public static class ModelProperties {
        private String modelId;
        private String displayName;
        private int maxTokens = 4096;
        private double temperature = 0.7;
        private double topP = 0.9;
        private int topK = 40;
        private String provider;
        private double costPer1kInputTokens;
        private double costPer1kOutputTokens;
        private int contextWindow = 100000;
        private boolean streamingSupported = true;
        private RateLimits rateLimits = new RateLimits();

        @Override
        public String toString() {
            return "ModelProperties{" +
                    "modelId='" + modelId + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", maxTokens=" + maxTokens +
                    ", temperature=" + temperature +
                    ", topP=" + topP +
                    ", topK=" + topK +
                    ", provider='" + provider + '\'' +
                    ", costPer1kInputTokens=" + costPer1kInputTokens +
                    ", costPer1kOutputTokens=" + costPer1kOutputTokens +
                    ", contextWindow=" + contextWindow +
                    ", streamingSupported=" + streamingSupported +
                    ", rateLimits=" + rateLimits +
                    '}';
        }

        // Getters and setters
        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getTopP() {
            return topP;
        }

        public void setTopP(double topP) {
            this.topP = topP;
        }

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public double getCostPer1kInputTokens() {
            return costPer1kInputTokens;
        }

        public void setCostPer1kInputTokens(double costPer1kInputTokens) {
            this.costPer1kInputTokens = costPer1kInputTokens;
        }

        public double getCostPer1kOutputTokens() {
            return costPer1kOutputTokens;
        }

        public void setCostPer1kOutputTokens(double costPer1kOutputTokens) {
            this.costPer1kOutputTokens = costPer1kOutputTokens;
        }

        public int getContextWindow() {
            return contextWindow;
        }

        public void setContextWindow(int contextWindow) {
            this.contextWindow = contextWindow;
        }

        public boolean isStreamingSupported() {
            return streamingSupported;
        }

        public void setStreamingSupported(boolean streamingSupported) {
            this.streamingSupported = streamingSupported;
        }

        public RateLimits getRateLimits() {
            return rateLimits;
        }

        public void setRateLimits(RateLimits rateLimits) {
            this.rateLimits = rateLimits;
        }
    }

    public static class RateLimits {
        private int requestsPerMinute = 50;
        private int tokensPerMinute = 40000;

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }

        public int getTokensPerMinute() {
            return tokensPerMinute;
        }

        public void setTokensPerMinute(int tokensPerMinute) {
            this.tokensPerMinute = tokensPerMinute;
        }

        @Override
        public String toString() {
            return "RateLimits{" +
                    "requestsPerMinute=" + requestsPerMinute +
                    ", tokensPerMinute=" + tokensPerMinute +
                    '}';
        }
    }
}