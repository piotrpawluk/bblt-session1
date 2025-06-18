package com.drfirst.bblt.session1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.example\\.bedrock\\.cli\\..*"
    )
)
@ConditionalOnProperty(name = "web.only", havingValue = "true", matchIfMissing = true)
public class BedrockWebApplication {
    
    public static void main(String[] args) {
        System.setProperty("server.port", "8911");
        System.setProperty("spring.main.web-application-type", "servlet");
        SpringApplication.run(BedrockWebApplication.class, args);
    }
}