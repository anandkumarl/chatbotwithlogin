import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import MessageList from './MessageList';
import './ChatBot.css';

const ChatBot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [selectedModel, setSelectedModel] = useState('gemini-2.5-flash');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const { logout } = useAuth();

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async () => {
    const userMessage = inputValue.trim();
    if (!userMessage) return;

    // Add user message to chat
    const newMessages = [...messages, { type: 'user', content: userMessage }];
    setMessages(newMessages);
    setInputValue('');
    setIsLoading(true);

    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({
          userMessage,
          model: selectedModel,
        }),
      });

      const data = await response.json();
      console.log('Server Response:', data);

      if (!response.ok || data.error) {
        const errorMsg = data.error || 'Server returned an error';
        console.error('Server Error:', errorMsg);
        setMessages([...newMessages, { type: 'bot', content: `Error: ${errorMsg}` }]);
      } else {
        const botMessage = data.botMessage || 'Unable to extract message';
        const source = data.source || 'unknown';
        console.log(`Response source: ${source} (${source === 'cache' ? '🔄 Cached' : '🌐 API'})`);
        setMessages([...newMessages, { type: 'bot', content: botMessage }]);
      }
    } catch (error) {
      console.error('Error fetching bot response:', error);
      setMessages([
        ...newMessages,
        { type: 'bot', content: 'Sorry, something went wrong. Please try again.' },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !isLoading) {
      handleSendMessage();
    }
  };

  const handleLogout = () => {
    logout();
    setIsOpen(false);
  };

  return (
    <>
      {/* Floating Chat Icon */}
      {!isOpen && (
        <div className="chatbot-icon" onClick={() => setIsOpen(true)}>
          💬
        </div>
      )}

      {/* Chatbot Container */}
      {isOpen && (
        <div className="chatbot-container">
          <div className="chatbot-header">
            <span>Chatbot</span>
            <div>
              <button className="logout-btn" onClick={handleLogout}>Logout</button>
              <button className="close-btn" onClick={() => setIsOpen(false)}>
                ×
              </button>
            </div>
          </div>

          <div className="chatbot-settings">
            <label htmlFor="chatbot-model">Model</label>
            <select
              id="chatbot-model"
              value={selectedModel}
              onChange={(e) => setSelectedModel(e.target.value)}
            >
              <option value="gemini-2.5-flash">Gemini 2.5 Flash</option>
              <option value="gemini-1.5-pro">Gemini 1.5 Pro</option>
              <option value="gemini-1.0">Gemini 1.0</option>
            </select>
          </div>

          <MessageList messages={messages} loadingRef={messagesEndRef} />

          <div className="chatbot-input-container">
            <input
              id="chatbot-input"
              type="text"
              placeholder="Type a message..."
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={isLoading}
            />
            <button
              id="send-btn"
              onClick={handleSendMessage}
              disabled={isLoading}
            >
              {isLoading ? 'Sending...' : 'Send'}
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default ChatBot;
