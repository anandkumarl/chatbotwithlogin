# React Frontend for RAG Chatbot

This is a React frontend for the RAG Chatbot Spring Boot application.

## Prerequisites

- Node.js 14+ and npm

## Installation

```bash
npm install
```

## Development

Start the development server:

```bash
npm start
```

This will start a development server on `http://localhost:3000` and proxy API requests to `http://localhost:8080`.

## Production Build

Build the application for production:

```bash
npm run build
```

This creates an optimized build in the `build` folder. The output can be served by Spring Boot.

## Integration with Spring Boot

To integrate the React build with Spring Boot:

1. Build the React app:
   ```bash
   npm run build
   ```

2. Copy the contents of the `build` folder to `../src/main/resources/static/`

3. The Spring Boot application will serve the React app at `http://localhost:8080`

## Components

- **App.js** - Main application component
- **ChatBot.js** - Main chatbot component with state management
- **MessageList.js** - Component to display chat messages

## Styling

- **App.css** - Application styles
- **ChatBot.css** - Chatbot component styles
- **index.css** - Global styles

## API Integration

The frontend communicates with the Spring Boot backend at `http://localhost:8080/api/chat`

Request format:
```json
{
  "userMessage": "Your message here",
  "model": "gemini-2.5-flash"
}
```

Response format:
```json
{
  "botMessage": "Bot response",
  "source": "cache",
  "model": "gemini-2.5-flash",
  "timestamp": 1712754632000
}
```

## Features

- Real-time chat interface
- Model selection (Gemini 2.5 Flash, Gemini 1.5 Pro, Gemini 1.0)
- Redis caching (responses cached for 24 hours)
- Responsive design
- Loading states
- Error handling
- Smooth animations

## Troubleshooting

### CORS Issues

If you see CORS errors in the browser console, ensure the Spring Boot backend is running and allows requests from `http://localhost:3000`.

The Spring Boot application already has CORS configured for all origins in development.

### Connection Refused

If you get "Connection refused" errors, make sure:
1. Spring Boot is running on `http://localhost:8080`
2. Redis is running on `localhost:6379`

## License

MIT
