package com.drfirst.bblt.session1.service;

import com.drfirst.bblt.session1.model.ChatRequest;
import com.drfirst.bblt.session1.model.ChatResponse;

/**
 * Interface for model invocation to break circular dependency between
 * BedrockErrorHandler and BedrockService
 */
public interface ModelInvoker {
    
    /**
     * Invoke a model directly without retry/fallback logic
     */
    ChatResponse invokeModelDirect(ChatRequest request);
}