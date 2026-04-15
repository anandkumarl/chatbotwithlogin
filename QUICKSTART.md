# Spring Boot RAG Chatbot - Quick Start Guide

## Prerequisites Checklist

- [ ] Java 17+ installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] Redis Server installed and ready to start
- [ ] Gemini API Key obtained from https://ai.google.dev/

## Step-by-Step Setup

### 1. Start Redis Server

**Windows (PowerShell):**
```powershell
redis-server
```

**Mac (Terminal):**
```bash
redis-server
```

**Linux (Terminal):**
```bash
redis-server
```

Keep this terminal open. Redis will run on `localhost:6379`

### 2. Configure API Key

Create `.env` file in project root with:
```
GEMINI_API_KEY=your_gemini_api_key_here
```

### 3. Open New Terminal/PowerShell in Project Root

Navigate to the project directory:
```bash
cd "d:\Code Base\RAG Chat Bot"
```

### 4. Build Project (First Time Only)

```bash
mvn clean install
```

This downloads all dependencies and compiles the code. May take 2-5 minutes.

### 5. Run the Application

```bash
mvn spring-boot:run
```

Wait for output:
```
Started RagChatbotApplication in 3.5 seconds
```

### 6. Test the Application

1. Visit **http://localhost:8080** in your browser
2. Click the chat icon (💬) in bottom-right
3. Type a message and send
4. Look for "🔄 Cached" or "🌐 API" in browser console (F12)

### 7. Verify Caching

Test caching by:
1. Send message: "What is AI?"
2. Check console: Should show source: "api"
3. Send same message again
4. Check console: Should show source: "cache" (much faster!)

## Running Commands

### Subsequent Runs (If Redis Already Running)

Just run:
```bash
mvn spring-boot:run
```

### Just Build Without Running

```bash
mvn clean package
```

Produces JAR at: `target/rag-chatbot-1.0.0.jar`

### Run the JAR Directly

```bash
java -jar target/rag-chatbot-1.0.0.jar
```

## Common Issues

### Redis Not Running
```
Error: Unable to connect to localhost:6379
```
→ Start Redis server in another terminal

### API Key Missing
```
Error: No API key provided
```
→ Create `.env` file with `GEMINI_API_KEY=your_key`

### Port 8080 In Use
```
Error: Port 8080 already in use
```
→ Edit `src/main/resources/application.properties`:
```
server.port=8081
```

### Build Error
```
[ERROR] COMPILATION ERROR
```
→ Verify Java version: `java -version` (should be 17+)

## File Locations

- **Frontend**: `src/main/resources/static/`
- **Backend Code**: `src/main/java/com/ragchatbot/`
- **Configuration**: `src/main/resources/application.properties`
- **JAR Output**: `target/rag-chatbot-1.0.0.jar`

## Logs Location

Logs appear in terminal output when running `mvn spring-boot:run`

Set log level in `application.properties`:
```
logging.level.com.ragchatbot=DEBUG
```

## Production Deployment

```bash
# Build
mvn clean package

# Run with API key
java -Dgemini.api.key=your_key -jar target/rag-chatbot-1.0.0.jar
```

Or with environment variable:
```bash
export GEMINI_API_KEY=your_key
java -jar target/rag-chatbot-1.0.0.jar
```

## Support Resources

- Spring Boot Docs: https://spring.io/projects/spring-boot
- Gemini API Docs: https://ai.google.dev/
- Redis Docs: https://redis.io/documentation
- Maven Docs: https://maven.apache.org/guides/

---

**Enjoy your RAG Chatbot! 🚀**
