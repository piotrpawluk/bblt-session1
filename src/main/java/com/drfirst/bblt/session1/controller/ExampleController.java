package com.drfirst.bblt.session1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ExampleController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "message", "Welcome to Spring AI with AWS Bedrock!",
            "endpoints", String.join(", ", 
                "/api/chat/health",
                "/api/chat/completion", 
                "/api/chat/completion/detailed",
                "/api/chat/completion/system"
            )
        );
    }
}