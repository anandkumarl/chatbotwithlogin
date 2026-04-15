# RAG Chatbot with Redis Caching

A chatbot application integrated with Gemini API and Redis caching for optimized LLM responses using **Spring Boot** backend and **React** frontend.

## Features

✅ **Redis Caching** - Responses are cached for 24 hours to reduce API calls
✅ **Gemini API Integration** - Uses Google's Gemini LLM for responses
✅ **Multiple Models** - Support for Gemini 2.5 Flash, 1.5 Pro, and 1.0
✅ **Cache Statistics** - Monitor cache performance
✅ **Cache Management** - Clear cache on demand
✅ **Spring Boot Backend** - Robust Java-based backend with Spring Data Redis
✅ **React Frontend** - Modern, responsive React UI with hooks

## Project Structure

```
RAG Chat Bot/
├── frontend/                                      # React application
│   ├── public/
│   │   └── index.html                            # HTML entry point
│   ├── src/
│   │   ├── index.js                              # React entry point
│   │   ├── index.css                             # Global styles
│   │   ├── App.js                                # Main application component
│   │   ├── App.css                               # App styles
│   │   └── components/
│   │       ├── ChatBot.js                        # Chatbot component
│   │       └── ChatBot.css                       # Chatbot styles
│   ├── package.json                              # React dependencies
│   └── README.md                                  # Frontend documentation
│
├── src/
│   ├── main/
│   │   ├── java/com/ragchatbot/
│   │   │   ├── RagChatbotApplication.java       # Main Spring Boot application
│   │   │   ├── config/
│   │   │   │   ├── RedisConfig.java             # Redis configuration
│   │   │   │   └── StaticResourceConfig.java    # Static resource configuration
│   │   │   ├── controller/
│   │   │   │   └── ChatController.java          # REST API endpoints
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java             # Chat business logic
│   │   │   │   └── GeminiService.java           # Gemini API integration
│   │   │   └── dto/
│   │   │       ├── ChatRequest.java             # Request DTO
│   │   │       ├── ChatResponse.java            # Response DTO
│   │   │       ├── ErrorResponse.java           # Error response DTO
│   │   │       └── HealthResponse.java          # Health check DTO
│   │   └── resources/
│   │       ├── static/                           # Built React files (auto-generated)
│   │       └── application.properties            # Spring Boot configuration
│
├── pom.xml                                        # Maven dependencies
├── .env                                           # Environment variables
├── build-and-deploy.ps1                          # PowerShell build script
├── build-and-deploy.sh                           # Bash build script
├── README.md                                      # This file
├── QUICKSTART.md                                  # Quick start guide
└── ARCHITECTURE.md                               # Architecture documentation
```

## Prerequisites

- **Java 17+** (JDK 17 or higher)
- **Maven 3.6+** (for building the project)
- **Node.js 14+** (for React frontend)
- **npm** (Node Package Manager)
- **Redis Server** (running locally or remotely)
- **Gemini API Key** (from Google AI Studio)

## Installation & Setup

### 1. Install Redis

**Windows:**
```bash
choco install redis-64
```

**Mac:**
```bash
brew install redis
```

**Linux:**
```bash
sudo apt-get install redis-server
```

### 2. Install Java and Maven

Verify installations:
```bash
java -version
mvn -version
```

### 3. Start Redis Server

**Windows:**
```bash
redis-server
```

**Mac/Linux:**
```bash
redis-server
```

Keep this terminal open.

### 4. Configure Environment Variables

Create a `.env` file in the project root:

```env
GEMINI_API_KEY=your_gemini_api_key_here
```

Get your Gemini API key from: https://ai.google.dev/

### 5. Build and Deploy React Frontend

Navigate to the project root and run the build script:

**Windows (PowerShell):**
```bash
.\build-and-deploy.ps1
```

**Mac/Linux (Bash):**
```bash
chmod +x build-and-deploy.sh
./build-and-deploy.sh
```

This script will:
1. Install React dependencies
2. Build the React application
3. Copy the build files to Spring Boot's static folder
4. Spring Boot will automatically serve the React app

### 6. Build Spring Boot Backend

```bash
mvn clean install
```

### 7. Run Spring Boot Application

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### 8. Access the Application

Open your browser and visit: **http://localhost:8080**

## Frontend Development (Optional)

If you want to develop the React frontend with hot-reload:

```bash
cd frontend
npm install
npm start
```

This starts a development server on `http://localhost:3000` with hot-reload enabled.

**Note:** The React dev server will proxy API requests to `http://localhost:8080`.

## How It Works

### Cache Flow

1. **User sends message** → Frontend sends to `http://localhost:8080/api/chat`
2. **Server checks Redis** → Cache hit? Return cached response
3. **Cache miss?** → Call Gemini API for new response
4. **Store in Redis** → Response cached for 24 hours
5. **Return response** → Frontend displays message

### Response Format

```json
{
  "botMessage": "Response text here",
  "source": "cache",
  "model": "gemini-2.5-flash",
  "timestamp": 1712754632000
}
```

## API Endpoints

All endpoints are served on `http://localhost:8080`

### 1. Chat Endpoint
```
POST /api/chat
```

Request:
```json
{
  "userMessage": "Hello, how are you?",
  "model": "gemini-2.5-flash"
}
```

Response:
```json
{
  "botMessage": "I'm doing well, thank you for asking!",
  "source": "cache",
  "model": "gemini-2.5-flash",
  "timestamp": 1712754632000
}
```

### 2. Health Check
```
GET /api/health
```

Response:
```json
{
  "status": "OK",
  "redis": true,
  "timestamp": 1712754632000
}
```

### 3. Clear Cache
```
POST /api/cache/clear
```

Request (optional, clears specific key):
```json
{
  "key": "gemini:gemini-2.5-flash:hello how are you"
}
```

Or leave empty to clear all cache:
```json
{}
```

### 4. Cache Statistics
```
GET /api/cache/stats
```

Response:
```json
{
  "dbSize": 5,
  "timestamp": 1712754632000
}
```

## Cache Key Structure

Cache keys are generated from the user message and model:

```
Format: gemini:{model}:{lowercase_message}
Example: gemini:gemini-2.5-flash:what is machine learning
```

## Configuration

### Application Properties (`application.properties`)

Edit `src/main/resources/application.properties` to customize:

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=

# Server Configuration
server.port=8080

# Logging
logging.level.root=INFO
logging.level.com.ragchatbot=DEBUG
```

### Cache Duration

The cache TTL (Time To Live) is set to 24 hours:

```java
// In ChatService.java
private static final long CACHE_TTL = 24 * 60 * 60; // 24 hours in seconds
```

To modify, edit the `CACHE_TTL` variable.

## Monitoring Cache

### View Cache Size
```bash
redis-cli dbsize
```

### View All Keys
```bash
redis-cli keys "*"
```

### View Specific Key
```bash
redis-cli get "gemini:gemini-2.5-flash:your message"
```

### Monitor Real-time Activity
```bash
redis-cli monitor
```

## Building for Production

### Create an Executable JAR

```bash
mvn clean package
```

This creates `target/rag-chatbot-1.0.0.jar`

### Run the JAR

```bash
java -jar target/rag-chatbot-1.0.0.jar
```

### Deploy with Environment Variables

```bash
# Windows
set GEMINI_API_KEY=your_key && java -jar target/rag-chatbot-1.0.0.jar

# Mac/Linux
export GEMINI_API_KEY=your_key && java -jar target/rag-chatbot-1.0.0.jar
```

## Troubleshooting

### Redis Connection Failed

**Error**: `Unable to connect to Redis`

**Solution**: Ensure Redis server is running
```bash
# Check if Redis is running
redis-cli ping
# Should return: PONG

# Start Redis if not running
redis-server
```

### API Key Invalid

**Error**: `Gemini API Error: Invalid API key`

**Solution**:
1. Get a new API key from https://ai.google.dev/
2. Update `.env` file with `GEMINI_API_KEY=your_key`

### Port 8080 Already in Use

**Solution**: Change the port in `application.properties`:
```properties
server.port=8081
```

Then access at `http://localhost:8081`

### Build Errors

**Error**: `[ERROR] COMPILATION ERROR`

**Solution**: Ensure Java 17+ is installed
```bash
java -version
```

Update `pom.xml` if using a different Java version:
```xml
<java.version>17</java.version>
```

### Maven Dependency Issues

**Error**: `Could not find artifact`

**Solution**: Update Maven
```bash
mvn -U clean install
```

## Performance Benefits

| Scenario | Without Cache | With Cache |
|----------|---------------|------------|
| 1st request | ~2-5 seconds | ~2-5 seconds |
| Identical 2nd request | ~2-5 seconds | <500ms |
| API calls for repeated questions | 100% API calls | ~95% reduction |

## Security Notes

⚠️ **Important**: Never commit sensitive data to version control

1. Add `.env` to `.gitignore`:
```
.env
target/
.idea/
*.iml
```

2. Always use environment variables for API keys
3. Restrict Redis access in production (use password protection):
   - Edit `application.properties`:
     ```properties
     spring.data.redis.password=your_redis_password
     ```
4. Use HTTPS for production deployments

## Development

### Enable Debug Logging

Edit `application.properties`:
```properties
logging.level.com.ragchatbot=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Hot Reload During Development

The project includes Spring DevTools for automatic restart:
```bash
mvn spring-boot:run
```

Changes to Java files will trigger automatic restart.

## Dependencies

- **Spring Boot 3.2.0** - Web framework
- **Spring Data Redis** - Redis integration
- **Lettuce** - Redis client
- **Lombok** - Reduce boilerplate code
- **Jackson** - JSON processing

## Next Steps

1. ✅ Install Java 17+, Maven, Node.js 14+
2. ✅ Install Redis
3. ✅ Configure `.env` file
4. ✅ Start Redis server
5. ✅ Run build script: `.\build-and-deploy.ps1` (Windows) or `./build-and-deploy.sh` (Mac/Linux)
6. ✅ Run `mvn clean install`
7. ✅ Start Spring Boot: `mvn spring-boot:run`
8. ✅ Open `http://localhost:8080` in browser
9. ✅ Test the chatbot

## Support

For issues or questions:
- Check Redis is running: `redis-cli ping`
- Check Spring Boot logs in console
- Check browser console for React errors (F12)
- Verify API key in `.env`
- Review [ARCHITECTURE.md](ARCHITECTURE.md) for system design

## License

MIT

