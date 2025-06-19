# AWS Bedrock Spring AI Demo - Week 1 Presentation

A comprehensive Spring Boot application demonstrating AWS Bedrock integration with Spring AI for Week 1 of the foundation models course.

## 📋 Week 1 Agenda Coverage

This project covers all topics from the Week 1 agenda:

### ✅ Goals Achieved
- [x] **AWS Bedrock Architecture**: Complete setup with Spring AI integration
- [x] **Foundation Models**: Claude 3.7, Claude 4.0, Amazon Nova Pro support
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
- Java 21+
- Maven 3.6+
- AWS Account with Bedrock access
- AWS CLI configured OR environment variables

### 1. Set AWS Credentials

#### Option 1: AWS CLI with SSO (Recommended for Organizations)

```bash
# Install/Update AWS CLI (if not already installed)
# macOS
brew install awscli
# or download from https://aws.amazon.com/cli/

# Configure SSO profile
aws configure sso
# Follow the prompts:
# - SSO session name: your-org-session
# - SSO start URL: https://your-org.awsapps.com/start
# - SSO region: us-east-1 (or your organization's region)
# - SSO registration scopes: sso:account:access
# - CLI default client Region: us-east-1
# - CLI default output format: json
# - CLI profile name: your-profile-name

# Login to your SSO session
aws sso login --profile your-profile-name

# Verify access to Bedrock
aws bedrock list-foundation-models --region us-east-1 --profile your-profile-name

# Set the profile as default (optional)
export AWS_PROFILE=your-profile-name

# Test Bedrock access with Claude models
aws bedrock list-foundation-models --region us-east-1 --query 'modelSummaries[?contains(modelId, `claude`)].{ModelId:modelId,ModelName:modelName}' --output table
```

#### Option 2: Traditional AWS Credentials

```bash
# Environment variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1

# Or use AWS CLI configure
aws configure
```

#### Option 3: AWS CLI with Access Keys

```bash
# Configure AWS CLI with access keys
aws configure
# AWS Access Key ID: your_access_key
# AWS Secret Access Key: your_secret_key
# Default region name: us-east-1
# Default output format: json
```

**Important Notes:**
- Ensure your AWS account has access to **Amazon Bedrock** service
- Enable access to **Claude 3.7 Sonnet** and **Claude 4.0 Opus** models in Bedrock console
- If using SSO, make sure to refresh credentials when they expire: `aws sso login --profile your-profile-name`

### 2. Google Vertex AI Setup (Alternative/Additional Provider)

This application also supports Google Vertex AI with Gemini models as an alternative or additional provider to AWS Bedrock.

#### Prerequisites for Vertex AI
- Google Cloud Project with billing enabled
- Vertex AI API enabled
- Service account with proper IAM roles
- Google Cloud CLI (gcloud) installed

#### Option 1: Service Account Key (Recommended for Development)

```bash
# Install Google Cloud CLI
# macOS
brew install google-cloud-sdk
# or download from https://cloud.google.com/sdk/docs/install

# Authenticate with Google Cloud
gcloud auth login

# Create a new project (or use existing)
gcloud projects create your-vertex-project-id
gcloud config set project your-vertex-project-id

# Enable Vertex AI API
gcloud services enable aiplatform.googleapis.com

# Create service account
gcloud iam service-accounts create vertex-ai-service \
    --description="Service account for Vertex AI integration" \
    --display-name="Vertex AI Service Account"

# Grant necessary IAM roles
gcloud projects add-iam-policy-binding your-vertex-project-id \
    --member="serviceAccount:vertex-ai-service@your-vertex-project-id.iam.gserviceaccount.com" \
    --role="roles/aiplatform.user"

gcloud projects add-iam-policy-binding your-vertex-project-id \
    --member="serviceAccount:vertex-ai-service@your-vertex-project-id.iam.gserviceaccount.com" \
    --role="roles/ml.developer"

# Create and download service account key
gcloud iam service-accounts keys create ~/vertex-ai-key.json \
    --iam-account=vertex-ai-service@your-vertex-project-id.iam.gserviceaccount.com

# Set environment variable
export GOOGLE_APPLICATION_CREDENTIALS=~/vertex-ai-key.json
export GOOGLE_VERTEX_PROJECT=your-vertex-project-id
export GOOGLE_VERTEX_LOCATION=us-central1
```

#### Option 2: Application Default Credentials (ADC)

```bash
# Install and initialize gcloud CLI
gcloud init

# Set application default credentials
gcloud auth application-default login

# Set project
gcloud config set project your-vertex-project-id

# Enable Vertex AI API
gcloud services enable aiplatform.googleapis.com

# Verify access to Vertex AI models
gcloud ai models list --region=us-central1
```

#### Option 3: Environment Variables

```bash
# Set Vertex AI configuration
export GOOGLE_VERTEX_PROJECT=your-vertex-project-id
export GOOGLE_VERTEX_LOCATION=us-central1
export GOOGLE_VERTEX_MODEL_ID=gemini-2.5-flash-preview-05-20

# If using service account key
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/service-account-key.json
```

#### Verify Vertex AI Setup

```bash
# Test Vertex AI access - list all available models
gcloud ai models list --region=us-central1

# Test Vertex AI API access and project authentication
gcloud auth print-access-token

# Verify your project and location are configured correctly
gcloud config get-value project
gcloud config get-value compute/region

# Test Gemini API with a simple content generation call
curl -X POST \
  -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  -H "Content-Type: application/json" \
  "https://us-central1-aiplatform.googleapis.com/v1/projects/$(gcloud config get-value project)/locations/us-central1/publishers/google/models/gemini-2.5-flash-preview-05-20:generateContent" \
  -d '{
    "contents": [{
      "role": "user",
      "parts": [{"text": "Hello! Can you confirm that Vertex AI is working?"}]
    }]
  }'
```

#### Vertex AI Configuration in Application

The application is already configured to use Vertex AI. Update these environment variables:

```bash
# Required Vertex AI variables
export GOOGLE_VERTEX_PROJECT=your-vertex-project-id
export GOOGLE_VERTEX_LOCATION=us-central1
export GOOGLE_VERTEX_MODEL_ID=gemini-2.5-flash-preview-05-20

# Optional: Service account key path
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
```

### 3. Clone and Build

```bash
git clone https://github.com/piotrpawluk/bblt-session1.git
cd bblt-session1
mvn clean package
```

### 4. Run the Application

```bash
# Web API mode (default)
mvn spring-boot:run

# CLI mode
java -jar target/session1-app-0.0.1-SNAPSHOT-cli.jar

# Web mode (alternative)
java -jar target/session1-app-0.0.1-SNAPSHOT-web.jar
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

### Google Vertex AI / Gemini
- `POST /api/gemini/chat` - Gemini chat completion
- `POST /api/gemini/chat/stream` - Streaming Gemini responses
- `GET /api/gemini/models` - Available Gemini models
- `POST /api/gemini/compare` - Compare Gemini vs Bedrock models

## 🛠 CLI Tool Usage

### Interactive Mode
```bash
java -jar app.jar -i

# Interactive commands:
[claude-3-7-sonnet]> Hello, tell me about quantum computing
[claude-3-7-sonnet]> /model claude-4-opus
[claude-4-opus]> /temperature 0.1
[claude-4-opus]> /compare What is artificial intelligence?
[claude-4-opus]> quit
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
java -jar app.jar benchmark --models claude-3-7-sonnet,claude-4-opus
```

### CLI Options
- `-m, --model <model>` - Specify model (default: claude-3-7-sonnet)
- `-t, --temperature <n>` - Set temperature 0.0-1.0 (default: 0.7)
- `-i, --interactive` - Interactive mode
- `-c, --compare` - Compare multiple models
- `--models <list>` - Models to compare (comma-separated)
- `-o, --output <file>` - Save output to file
- `-h, --help` - Show help message

## 📊 Supported Models

| Model | Provider | Context Window | Cost (per 1K tokens) | Best For |
|-------|----------|----------------|---------------------|----------|
| Claude 3.7 Sonnet | Anthropic/AWS | 200K | $0.003/$0.015 | Enhanced reasoning |
| Claude 4.0 Opus | Anthropic/AWS | 200K | $0.015/$0.075 | Most capable |
| Nova Pro | Amazon | 300K | $0.0008/$0.0032 | AWS native |
| Titan Express | Amazon | 8K | $0.0002/$0.0006 | Cost-effective |
| Gemini 2.5 Flash | Google/Vertex | 2M | $0.000075/$0.0003 | Ultra-fast, massive context |

**Note:** Costs shown as Input/Output tokens. Vertex AI models may have different pricing in different regions.

## 🎯 Prompt Engineering Examples

### Few-Shot Learning
```bash
curl -X POST "http://localhost:8911/api/prompt-engineering/few-shot" \
  -d "query=This movie was amazing!" \
  -d "domain=sentiment" \
  -d "modelId=claude-3-7-sonnet"
```

### Chain of Thought
```bash
curl -X POST "http://localhost:8911/api/prompt-engineering/chain-of-thought" \
  -d "problem=If a train travels 120 miles in 2 hours, what is its average speed?" \
  -d "modelId=claude-3-7-sonnet"
```

### Code Review
```bash
curl -X POST "http://localhost:8911/api/prompt-engineering/code-review" \
  -d "code=public class Example { public static void main(String[] args) { System.out.println(\"Hello\"); } }" \
  -d "language=Java" \
  -d "modelId=claude-3-7-sonnet"
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

# Google Vertex AI Configuration
GOOGLE_VERTEX_PROJECT=your-project-id
GOOGLE_VERTEX_LOCATION=us-central1
GOOGLE_VERTEX_MODEL_ID=gemini-2.5-flash-preview-05-20
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json

# Model Configuration
CLAUDE_MODEL_ID=us.us.anthropic.claude-3-7-sonnet-20250219-v1:0
CLAUDE_4_MODEL_ID=us.anthropic.claude-opus-4-20250514-v1:0
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
  "modelId": "claude-3-7-sonnet",
  "temperature": 0.7,
  "maxTokens": 1000,
  "includeMetrics": true
}
```

### Model Comparison
```bash
POST /api/models/compare?message=Write a haiku about AI&modelIds=claude-3-7-sonnet,claude-4-opus,nova-pro
```

### Streaming Response
```bash
curl -N -X POST "http://localhost:8911/api/chat/completion/stream" \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me a story", "modelId": "claude-3-7-sonnet"}'
```

### Vertex AI / Gemini Examples
```bash
# Gemini chat completion
curl -X POST "http://localhost:8911/api/gemini/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain quantum computing", "temperature": 0.7, "maxTokens": 1000}'

# Compare AWS Bedrock vs Google Vertex AI
curl -X POST "http://localhost:8911/api/gemini/compare" \
  -H "Content-Type: application/json" \
  -d '{"message": "Write a haiku about AI", "bedrockModel": "claude-3-7-sonnet"}'
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
curl -X POST "http://localhost:8911/api/models/benchmark?modelIds=claude-3-7-sonnet,claude-4-opus"
```

Sample benchmark output:
```
┌─────────────────────┬─────────────┬────────────┬──────────┬──────────────┐
│ Model               │ Time (ms)   │ Tokens     │ Cost ($) │ Speed (t/s)  │
├─────────────────────┼─────────────┼────────────┼──────────┼──────────────┤
│ Claude 3.7 Sonnet   │        1200 │         89 │ 0.001245 │        74.17 │
│ Claude 4.0 Opus     │        1600 │        112 │ 0.008400 │        70.00 │
│ Nova Pro            │         950 │         78 │ 0.000624 │        82.11 │
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

2. **Google Cloud Authentication Errors**
   ```bash
   # Set service account credentials
   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
   
   # Or use application default credentials
   gcloud auth application-default login
   
   # Verify project access
   gcloud projects describe your-project-id
   ```

3. **Vertex AI API Not Enabled**
   ```bash
   gcloud services enable aiplatform.googleapis.com
   gcloud services list --enabled --filter="name:aiplatform.googleapis.com"
   ```

4. **Model Not Available**
   - Check AWS Bedrock model access in your region
   - Verify Vertex AI model availability in your region
   - Verify model IDs in configuration

5. **Vertex AI Quota/Billing Issues**
   - Check project billing is enabled: `gcloud billing projects describe your-project-id`
   - Review Vertex AI quotas in Google Cloud Console
   - Verify service account has `aiplatform.user` and `ml.developer` roles

6. **Port Conflict**
   - Application runs on port 8911 by default
   - Change with `server.port=8912` in application.yml

7. **Memory Issues**
   - Increase JVM heap: `-Xmx2g`
   - Enable streaming for large responses

8. **Region Mismatch**
   - Ensure Vertex AI location matches your intended region
   - Some Gemini models are only available in specific regions
   - Use `gcloud config get-value compute/region` to check current region

9. **Vertex AI API Endpoint Issues**
   - The correct endpoint format is: `https://REGION-aiplatform.googleapis.com/v1/projects/PROJECT/locations/LOCATION/publishers/google/models/MODEL:generateContent`
   - For listing models, use: `gcloud ai models list --region=REGION`
   - Test API access with: `gcloud auth print-access-token`

### Support

- Check application logs in `logs/` directory
- View health endpoint: `GET /actuator/health`
- Enable debug logging: `logging.level.com.drfirst.bblt.session1=DEBUG`

---

**Week 1 Assignment Complete!** 🎉

This comprehensive demo covers all Week 1 objectives and provides a solid foundation for advanced AWS Bedrock development with Spring AI.
