# RAG Chatbot - Spring Boot Architecture

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Web Browser                               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Frontend (HTML/CSS/JavaScript)                         │   │
│  │  - index.html / styles.css / script.js                  │   │
│  │  - Fetches: POST /api/chat                              │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────┬──────────────────────────────────────────────────────┘
             │ HTTP (localhost:8080)
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  ChatController (/api/chat)                             │   │
│  │  ├─ POST /api/chat          → Handle chat requests      │   │
│  │  ├─ GET /api/health         → Health check              │   │
│  │  ├─ POST /api/cache/clear   → Clear cache               │   │
│  │  └─ GET /api/cache/stats    → Get cache statistics      │   │
│  └──────────────────────────────────────────────────────────┘   │
│               ▲                              ▲                   │
│               │                              │                   │
│  ┌────────────▼──────────────────┐  ┌───────┴────────────────┐  │
│  │  ChatService                  │  │  GeminiService         │  │
│  │  - getChatResponse()          │  │  - callGeminiAPI()    │  │
│  │  - generateCacheKey()         │  │  - extractMessage()   │  │
│  │  - clearCacheKey()            │  │                        │  │
│  │  - getCacheSize()             │  │                        │  │
│  └────────────┬──────────────────┘  └───────────────────────┘  │
│               │                                │                 │
│               │ Spring Data Redis              │                 │
│               │                    HTTP POST   │                 │
│  ┌────────────▼──────────────────┐            │                 │
│  │  RedisTemplate                │            │                 │
│  │  - SET/GET operations         │            │                 │
│  │  - Cache management           │            │                 │
│  └──────────────────────────────┘            │                 │
└─────────────────────────────────────┬─────────┼──────────────────┘
                                      │         │
        ┌─────────────────────────────┘         │
        │                                       │
        ▼                                       ▼
┌──────────────────┐          ┌────────────────────────────────────┐
│  Redis Server    │          │  Gemini API                        │
│  localhost:6379  │          │  https://generativelanguage...     │
│                  │          │                                    │
│  - Cache Storage │          │  - LLM Processing                 │
│  - 24hr TTL      │          │  - Returns bot response           │
│  - 86400 seconds │          │                                    │
└──────────────────┘          └────────────────────────────────────┘
```

## Request Flow

### New Message (Cache Miss)

```
1. User sends message in browser
   │
2. Frontend → POST /api/chat (userMessage, model)
   │
3. ChatController receives request
   │
4. ChatService.getChatResponse()
   │
5. Check Redis cache
   │
   └─ MISS (not found)
   │
6. GeminiService.callGeminiAPI()
   │
7. HTTP POST to Gemini API
   │
8. Gemini returns LLM response
   │
9. ChatService stores in Redis (24hr TTL)
   │
10. Return response with source: "api"
   │
11. Frontend displays message with console log: 🌐 API
```

### Cached Message (Cache Hit)

```
1. User sends message in browser
   │
2. Frontend → POST /api/chat (userMessage, model)
   │
3. ChatController receives request
   │
4. ChatService.getChatResponse()
   │
5. Check Redis cache
   │
   └─ HIT (found!)
   │
6. Return cached response immediately
   │
7. Return response with source: "cache"
   │
8. Frontend displays message with console log: 🔄 Cached
```

## Class Responsibilities

### Controllers
- **ChatController**: REST endpoint handlers
  - Routes HTTP requests to services
  - Response formatting
  - Error handling
  - CORS configuration

### Services
- **ChatService**: Business logic
  - Cache key generation
  - Redis cache management
  - Orchestration between GeminiService and Redis
  
- **GeminiService**: API integration
  - Gemini API communication
  - Request/response parsing
  - Error handling for external API

### Configuration
- **RedisConfig**: Redis template setup
  - String serialization
  - Connection pooling
  
- **StaticResourceConfig**: Frontend serving
  - Maps static resources to classpath:/static/
  
- **WebConfig** (in RagChatbotApplication): CORS setup
  - Allow cross-origin requests

### Data Transfer Objects (DTOs)
- **ChatRequest**: Incoming user messages
- **ChatResponse**: Cache hit response
- **ErrorResponse**: Error messages
- **HealthResponse**: Health check response

## Cache Strategy

### Cache Key Format
```
gemini:{model}:{lowercase_message}

Example:
gemini:gemini-2.5-flash:what is machine learning
```

### Key Benefits
1. **Model-specific** - Different models cache separately
2. **Consistent** - Case-insensitive for same questions
3. **Identifiable** - Easy to debug and monitor

### TTL (Time To Live)
```
24 hours = 86,400 seconds

After 24 hours, Redis automatically deletes the key
```

## Technology Stack

```
┌─ Frontend
│  ├─ HTML5
│  ├─ CSS3
│  └─ JavaScript (ES6+)
│
├─ Backend
│  ├─ Java 17
│  └─ Spring Boot 3.2.0
│     ├─ Spring Web (REST APIs)
│     ├─ Spring Data Redis
│     └─ Lombok (code generation)
│
├─ Database/Cache
│  └─ Redis (in-memory cache)
│
├─ External APIs
│  └─ Google Gemini API
│
└─ Build Tools
   ├─ Maven 3.6+
   ├─ Maven Compiler Plugin
   └─ Spring Boot Maven Plugin
```

## Deployment Architecture

### Development
```
┌─ Developer Machine
   ├─ Redis Server (localhost:6379)
   └─ Spring Boot App (localhost:8080)
```

### Production
```
┌─ Load Balancer (optional)
   │
   ├─ Spring Boot Instance 1 (8080)
   ├─ Spring Boot Instance 2 (8080)
   └─ Spring Boot Instance 3 (8080)
      │
      └─ Shared Redis Server (production-redis:6379)
```

## API Response Times

### Scenario: User asks "What is AI?"

**First Time (Cache Miss):**
- Frontend → Spring Boot: 10ms
- Spring Boot → Redis check: 5ms (miss)
- Spring Boot → Gemini API: 2000-5000ms (API processing)
- Redis cache write: 5ms
- Spring Boot → Frontend: 10ms
- **Total: 2025-5030ms (~2-5 seconds)**

**Second Time (Cache Hit):**
- Frontend → Spring Boot: 10ms
- Spring Boot → Redis check: 5ms (hit!)
- Spring Boot → Frontend: 10ms
- **Total: 25ms (~0.025 seconds)**

**Improvement: 80-200x faster!**

## Monitoring Health

### Endpoints for Monitoring
```
GET /api/health
└─ Check if app and Redis are healthy

GET /api/cache/stats
└─ Monitor cache size

POST /api/cache/clear
└─ Manual cache cleanup
```

### Log Levels (configurable)
```
DEBUG   - Detailed information for debugging
INFO    - General application flow (default)
WARN    - Warning messages
ERROR   - Error messages
```

## Security Considerations

1. **API Keys**: Stored in `.env`, not in code
2. **CORS**: Configured to allow necessary origins
3. **Input Validation**: Checks for empty messages
4. **Error Handling**: Doesn't expose internal errors
5. **Redis**: Can be password-protected in production

## Scaling Considerations

### Horizontal Scaling
- Multiple Spring Boot instances
- Shared Redis instance
- Load balancer for distribution

### Vertical Scaling
- Increase Java heap size: `-Xmx2g`
- Increase Redis memory
- Optimize database queries

### Caching Strategy
- Current: 24-hour TTL for all responses
- Future: Implement cache invalidation per user
- Consider: Distributed caching across multiple Redis instances

---

**Architecture Maintained by:** RAG Chatbot Team
**Last Updated:** 2026-04-10
