import React from 'react';
import './MessageList.css';

const MessageList = ({ messages, loadingRef }) => {
  return (
    <div className="chatbot-messages">
      {messages.map((msg, index) => (
        <div key={index} className={`message ${msg.type}`}>
          {msg.content}
        </div>
      ))}
      <div ref={loadingRef} />
    </div>
  );
};

export default MessageList;
