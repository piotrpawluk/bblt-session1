package com.drfirst.bblt.session1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockConfig {

    @Value("${spring.ai.bedrock.aws.region:us-east-1}")
    private String awsRegion;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(AwsCredentialsProvider credentialsProvider) {
        return BedrockRuntimeClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

}