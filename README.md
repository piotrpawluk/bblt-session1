# AWS Bedrock Spring AI Demo - Week 1 Presentation

A comprehensive Spring Boot application demonstrating AWS Bedrock integration with Spring AI for Week 1 of the foundation models course.

## 📋 Week 1 Agenda Coverage

This project covers all topics from the Week 1 agenda:

### ✅ Goals Achieved
- [x] **AWS Bedrock Architecture**: Complete setup with Spring AI integration
- [x] **Foundation Models**: Claude 3/3.5, Meta Llama 2, Amazon Titan support
- [x] **API Authentication**: AWS credentials and SDK configuration
- [x] **First LLM API Calls**: Multiple endpoints for Claude 3.7 and 4.0
- [x] **Prompt Engineering**: Few-shot learning, chain-of-thought, role-playing
- [x] **Token Limits & Pricing**: Cost estimation and rate limiting
- [x] **Performance Monitoring**: Response times, token usage, throughput

### 🎯 Core Topics Implemented
1. **AWS Credentials & Permissions** - Environment-based configuration
2. **Model Exploration** - 4 different foundation models with detailed comparison
3. **API Patterns** - Completion, chat, streaming responses with error handling
4. **Prompt Engineering** - Multiple techniques with practical examples
5. **Error Handling & Retry** - Comprehensive error management and logging

### 📚 Homework Assignment
**CLI Tool Features:**
- ✅ Connects to multiple Bedrock models
- ✅ Code reviewer functionality
- ✅ Recipe generator
- ✅ Story writer
- ✅ Model performance comparison and documentation
- ✅ Streaming responses with proper error handling

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- AWS Account with Bedrock access
- AWS CLI configured OR environment variables

### 1. Set AWS Credentials

```bash
# Option 1: Environment variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1

# Option 2: AWS CLI
aws configure
```

### 2. Clone and Build

```bash
git clone <repository-url>
cd bedrock-spring-ai-demo
mvn clean package
```

### 3. Run the Application

```bash
# Web API mode (default)
mvn spring-boot:run

# CLI mode
java -jar target/bedrock-spring-ai-demo-0.0.1-SNAPSHOT.jar -Dbedrock.cli.enabled=true

# Interactive CLI mode
java -jar target/bedrock-spring-ai-demo-0.0.1-SNAPSHOT.jar -Dbedrock.cli.enabled=true -i
```

## 🌐 API Endpoints

The application runs on port **8911** (configurable) and provides these endpoints:

### Health & Info
- `GET /api/chat/health` - Health check and available models
- `GET /api/chat/models` - List all available models
- `GET /api/chat/models/{modelId}/info` - Model-specific information

### Chat Completion
- `POST /api/chat/completion` - Basic chat completion
- `POST /api/chat/completion/detailed` - Chat with performance metrics
- `POST /api/chat/completion/stream` - Streaming chat responses
- `POST /api/chat/completion/system` - Chat with system prompts

### Model Comparison
- `POST /api/models/compare` - Compare multiple models
- `POST /api/models/benchmark` - Comprehensive benchmarking
- `GET /api/models/performance/summary` - Performance overview

### Prompt Engineering
- `GET /api/prompt-engineering/techniques` - List available techniques
- `POST /api/prompt-engineering/few-shot` - Few-shot learning examples
- `POST /api/prompt-engineering/chain-of-thought` - Step-by-step reasoning
- `POST /api/prompt-engineering/code-review` - Code review automation
- `POST /api/prompt-engineering/recipe-generator` - Recipe creation
- `POST /api/prompt-engineering/story-writer` - Creative writing
- `POST /api/prompt-engineering/temperature-test` - Temperature effects
- `POST /api/prompt-engineering/prompt-optimization` - Prompt improvement

## 🛠 CLI Tool Usage

### Interactive Mode
```bash
java -jar app.jar -i

# Interactive commands:
[claude-3-sonnet]> Hello, tell me about quantum computing
[claude-3-sonnet]> /model claude-3-5-sonnet
[claude-3-5-sonnet]> /temperature 0.1
[claude-3-5-sonnet]> /compare What is artificial intelligence?
[claude-3-5-sonnet]> quit
```

### Direct Commands
```bash
# Chat mode
java -jar app.jar chat

# Code review
java -jar app.jar code-review src/main/java/Example.java

# Recipe generation
java -jar app.jar recipe "chicken, rice, vegetables"

# Story writing
java -jar app.jar story sci-fi "AI consciousness"

# Model benchmarking
java -jar app.jar benchmark --models claude-3-sonnet,llama3-70b
```

### CLI Options
- `-m, --model <model>` - Specify model (default: claude-3-sonnet)
- `-t, --temperature <n>` - Set temperature 0.0-1.0 (default: 0.7)
- `-i, --interactive` - Interactive mode
- `-c, --compare` - Compare multiple models
- `--models <list>` - Models to compare (comma-separated)
- `-o, --output <file>` - Save output to file
- `-h, --help` - Show help message

## 📊 Supported Models

| Model | Provider | Context Window | Cost (per 1K tokens) | Best For |
|-------|----------|----------------|---------------------|----------|
| Claude 3 Sonnet | Anthropic | 200K | $0.003/$0.015 | General purpose |
| Claude 3.5 Sonnet | Anthropic | 200K | $0.003/$0.015 | Latest features |
| Llama 2 70B | Meta | 4K | $0.00195/$0.00256 | Cost-effective |
| Titan Express | Amazon | 8K | $0.0002/$0.0006 | Fast responses |

## 🎯 Prompt Engineering Examples

### Few-Shot Learning
```bash
curl -X POST "http://localhost:8911/api/prompt-engineering/few-shot" \
  -d "query=This movie was amazing!" \
  -d "domain=sentiment" \
  -d "modelId=claude-3-sonnet"
```

### Chain of Thought
```bash
curl -X POST "http://localhost:8911/api/prompt-engineering/chain-of-thought" \
  -d "problem=If a train travels 120 miles in 2 hours, what is its average speed?" \
  -d "modelId=claude-3-sonnet"
```

### Code Review
```bash
curl -X POST "http://localhost:8911/api/prompt-engineering/code-review" \
  -d "code=public class Example { public static void main(String[] args) { System.out.println(\"Hello\"); } }" \
  -d "language=Java" \
  -d "modelId=claude-3-sonnet"
```

## 📈 Performance Monitoring

The application includes comprehensive monitoring:

- **Response Times** - Latency tracking for each model
- **Token Usage** - Input/output token counting
- **Cost Estimation** - Real-time cost calculation
- **Throughput** - Tokens per second measurement
- **Error Rates** - Success/failure tracking

Access metrics at:
- `GET /actuator/health` - Application health
- `GET /actuator/metrics` - Detailed metrics
- `GET /actuator/info` - Application information

## 🔧 Configuration

### Environment Variables
```bash
# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_key
AWS_SECRET_ACCESS_KEY=your_secret

# Model Configuration
CLAUDE_MODEL_ID=anthropic.claude-3-sonnet-20240229-v1:0
LLAMA_MODEL_ID=meta.llama3-70b-instruct-v1:0
TITAN_MODEL_ID=amazon.titan-text-express-v1

# Application Configuration
CLI_ENABLED=true
SPRING_PROFILES_ACTIVE=dev
```

### Custom Model Configuration
Edit `application.yml` to add or modify model configurations:

```yaml
bedrock:
  models:
    custom-model:
      model-id: your.custom.model.id
      display-name: Custom Model
      provider: Provider Name
      max-tokens: 4096
      cost-per-1k-input-tokens: 0.001
      cost-per-1k-output-tokens: 0.002
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Integration tests (requires AWS credentials)
mvn test -Dtest=*IntegrationTest
```

## 📝 Example Requests

### Basic Chat
```json
POST /api/chat/completion
{
  "message": "Explain quantum computing in simple terms",
  "modelId": "claude-3-sonnet",
  "temperature": 0.7,
  "maxTokens": 1000,
  "includeMetrics": true
}
```

### Model Comparison
```bash
POST /api/models/compare?message=Write a haiku about AI&modelIds=claude-3-sonnet,claude-3-5-sonnet,llama3-70b
```

### Streaming Response
```bash
curl -N -X POST "http://localhost:8911/api/chat/completion/stream" \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me a story", "modelId": "claude-3-sonnet"}'
```

## 🚨 Error Handling

The application includes comprehensive error handling:

- **AWS Credential Errors** - Clear messaging for authentication issues
- **Model Availability** - Graceful fallback when models are unavailable
- **Rate Limiting** - Automatic retry with exponential backoff
- **Token Limits** - Request truncation and warnings
- **Network Issues** - Connection timeout and retry logic

## 📊 Performance Benchmarks

Run benchmarks to compare models:

```bash
# Quick benchmark
curl -X POST "http://localhost:8911/api/models/benchmark"

# Custom benchmark
curl -X POST "http://localhost:8911/api/models/benchmark?modelIds=claude-3-sonnet,llama3-70b"
```

Sample benchmark output:
```
┌─────────────────────┬─────────────┬────────────┬──────────┬──────────────┐
│ Model               │ Time (ms)   │ Tokens     │ Cost ($) │ Speed (t/s)  │
├─────────────────────┼─────────────┼────────────┼──────────┼──────────────┤
│ Claude 3 Sonnet     │        1250 │         89 │ 0.001245 │        71.20 │
│ Claude 3.5 Sonnet   │        1100 │         92 │ 0.001289 │        83.64 │
│ Llama 2 70B Chat    │        1800 │         76 │ 0.000195 │        42.22 │
└─────────────────────┴─────────────┴────────────┴──────────┴──────────────┘
```

## 🎓 Learning Resources

This project demonstrates:

1. **Spring AI Integration** - Production-ready AWS Bedrock setup
2. **Prompt Engineering** - Multiple techniques with real examples
3. **Model Comparison** - Performance and cost analysis tools
4. **Error Handling** - Robust production practices
5. **Monitoring** - Comprehensive metrics and logging
6. **CLI Development** - Interactive command-line tools

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Submit a pull request

## 📄 License

This project is for educational purposes as part of the Week 1 foundation models course.

## 🆘 Troubleshooting

### Common Issues

1. **AWS Credentials Not Found**
   ```bash
   export AWS_ACCESS_KEY_ID=your_key
   export AWS_SECRET_ACCESS_KEY=your_secret
   ```

2. **Model Not Available**
   - Check AWS Bedrock model access in your region
   - Verify model IDs in configuration

3. **Port Conflict**
   - Application runs on port 8911 by default
   - Change with `server.port=8912` in application.yml

4. **Memory Issues**
   - Increase JVM heap: `-Xmx2g`
   - Enable streaming for large responses

### Support

- Check application logs in `logs/` directory
- View health endpoint: `GET /actuator/health`
- Enable debug logging: `logging.level.com.drfirst.bblt.session1=DEBUG`

---

**Week 1 Assignment Complete!** 🎉

This comprehensive demo covers all Week 1 objectives and provides a solid foundation for advanced AWS Bedrock development with Spring AI.
