import React, { useEffect, useRef, useState } from 'react';
import { aiApi } from '../api/aiApi';
import { ChatMessage } from '../types';

const QUICK_PROMPTS = [
  'Tư vấn laptop văn phòng mỏng nhẹ dưới 20 triệu',
  'Laptop sinh viên học tập pin trâu, bền bỉ',
  'Laptop gaming / đồ họa cấu hình cao, RAM 16GB+',
];

export const AIChatWidget: React.FC = () => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      text: 'Xin chào! Tôi là Trợ lý AI LaptopStore. Tôi có thể tư vấn mẫu laptop phù hợp với nhu cầu, tầm giá và cấu hình bạn cần. Hãy đặt câu hỏi cho tôi nhé!',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [input, setInput] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSendMessage = async (promptToSend?: string) => {
    const text = (promptToSend || input).trim();
    if (!text || loading) return;

    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const response = await aiApi.chat({ prompt: text });
      const aiMsg: ChatMessage = {
        id: `ai-${Date.now()}`,
        sender: 'assistant',
        text: response.reply,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch {
      const errorMsg: ChatMessage = {
        id: `err-${Date.now()}`,
        sender: 'assistant',
        text: 'Rất tiếc, hiện tại không thể kết nối tới máy chủ AI. Bạn vui lòng thử lại sau ít phút nhé.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ position: 'fixed', bottom: '1.5rem', right: '1.5rem', zIndex: 9999 }}>
      {isOpen ? (
        <div
          style={{
            width: 380,
            height: 520,
            backgroundColor: '#ffffff',
            borderRadius: 16,
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
            display: 'flex',
            flexDirection: 'column',
            border: '1px solid #e2e8f0',
            overflow: 'hidden',
          }}
        >
          {/* Header */}
          <div
            style={{
              padding: '1rem',
              backgroundColor: '#2563eb',
              color: '#ffffff',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
              <div
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: '50%',
                  backgroundColor: '#ffffff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '1.25rem',
                }}
              >
                🤖
              </div>
              <div>
                <strong style={{ fontSize: '0.9375rem', display: 'block' }}>Trợ lý AI LaptopStore</strong>
                <span style={{ fontSize: '0.6875rem', color: '#bfdbfe' }}>Tư vấn cấu hình & kho hàng</span>
              </div>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              style={{
                background: 'none',
                border: 'none',
                color: '#ffffff',
                fontSize: '1.25rem',
                cursor: 'pointer',
                padding: '0.25rem',
              }}
            >
              ✕
            </button>
          </div>

          {/* Quick prompts */}
          <div
            style={{
              padding: '0.625rem 0.75rem',
              backgroundColor: '#f8fafc',
              borderBottom: '1px solid #f1f5f9',
              display: 'flex',
              gap: '0.375rem',
              overflowX: 'auto',
              whiteSpace: 'nowrap',
            }}
          >
            {QUICK_PROMPTS.map((qp, idx) => (
              <button
                key={idx}
                disabled={loading}
                onClick={() => handleSendMessage(qp)}
                style={{
                  fontSize: '0.6875rem',
                  padding: '0.25rem 0.625rem',
                  backgroundColor: '#ffffff',
                  border: '1px solid #cbd5e1',
                  borderRadius: 999,
                  color: '#334155',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  flexShrink: 0,
                }}
              >
                {qp}
              </button>
            ))}
          </div>

          {/* Message List */}
          <div
            style={{
              flex: 1,
              padding: '1rem',
              overflowY: 'auto',
              display: 'flex',
              flexDirection: 'column',
              gap: '0.75rem',
              backgroundColor: '#fdfdfd',
            }}
          >
            {messages.map((m) => (
              <div
                key={m.id}
                style={{
                  alignSelf: m.sender === 'user' ? 'flex-end' : 'flex-start',
                  maxWidth: '82%',
                }}
              >
                <div
                  style={{
                    padding: '0.75rem 0.875rem',
                    borderRadius: 12,
                    fontSize: '0.875rem',
                    lineHeight: 1.45,
                    backgroundColor: m.sender === 'user' ? '#2563eb' : '#f1f5f9',
                    color: m.sender === 'user' ? '#ffffff' : '#0f172a',
                    borderBottomRightRadius: m.sender === 'user' ? 2 : 12,
                    borderBottomLeftRadius: m.sender === 'assistant' ? 2 : 12,
                    whiteSpace: 'pre-line',
                  }}
                >
                  {m.text}
                </div>
                <div
                  style={{
                    fontSize: '0.625rem',
                    color: '#94a3b8',
                    marginTop: 2,
                    textAlign: m.sender === 'user' ? 'right' : 'left',
                  }}
                >
                  {m.timestamp}
                </div>
              </div>
            ))}

            {loading && (
              <div style={{ alignSelf: 'flex-start', display: 'flex', alignItems: 'center', gap: '0.375rem', padding: '0.5rem 0.75rem', backgroundColor: '#f1f5f9', borderRadius: 12 }}>
                <span style={{ fontSize: '0.75rem', color: '#64748b' }}>Trợ lý đang suy nghĩ và tra cứu kho...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Box */}
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSendMessage();
            }}
            style={{
              padding: '0.75rem',
              borderTop: '1px solid #e2e8f0',
              display: 'flex',
              gap: '0.5rem',
              backgroundColor: '#ffffff',
            }}
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Nhập câu hỏi tư vấn laptop..."
              disabled={loading}
              style={{
                flex: 1,
                padding: '0.625rem 0.875rem',
                border: '1px solid #cbd5e1',
                borderRadius: 8,
                fontSize: '0.875rem',
                outline: 'none',
              }}
            />
            <button
              type="submit"
              disabled={loading || !input.trim()}
              style={{
                padding: '0.625rem 1rem',
                backgroundColor: loading || !input.trim() ? '#93c5fd' : '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: 8,
                fontWeight: 600,
                fontSize: '0.875rem',
                cursor: loading || !input.trim() ? 'not-allowed' : 'pointer',
              }}
            >
              Gửi
            </button>
          </form>
        </div>
      ) : (
        <button
          onClick={() => setIsOpen(true)}
          style={{
            padding: '0.875rem 1.25rem',
            backgroundColor: '#2563eb',
            color: '#ffffff',
            borderRadius: 999,
            border: 'none',
            boxShadow: '0 10px 15px -3px rgba(37, 99, 235, 0.4)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontWeight: 700,
            fontSize: '0.9375rem',
            transition: 'transform 0.2s',
          }}
        >
          <span style={{ fontSize: '1.25rem' }}>🤖</span>
          <span>Tư vấn AI</span>
        </button>
      )}
    </div>
  );
};
