#!/bin/bash

echo "=== Testing Real Fallback Mechanism ==="
echo ""

echo "1. Testing invalid model (should trigger fallback to claude-3-7-sonnet):"
curl -s -X POST http://localhost:8911/api/chat/completion \
  -H "Content-Type: application/json" \
  -d '{"message": "What is 2+2?", "modelId": "invalid-model", "maxTokens": 50}' | jq -r '.content' | head -3
echo ""

echo "2. Checking circuit breaker status:"
curl -s http://localhost:8911/api/chat/circuit-breaker-status | jq -r '.circuitBreakerStatus'
echo ""

echo "3. Testing another invalid model (should trigger different fallback):"
curl -s -X POST http://localhost:8911/api/chat/completion \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the capital of France?", "modelId": "another-invalid-model", "maxTokens": 50}' | jq -r '.content' | head -3
echo ""

echo "4. Final circuit breaker status:"
curl -s http://localhost:8911/api/chat/circuit-breaker-status | jq -r '.circuitBreakerStatus'