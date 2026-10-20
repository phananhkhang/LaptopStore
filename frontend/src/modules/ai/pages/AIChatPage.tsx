import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { aiApi } from '../api/aiApi';
import { ChatMessage } from '../types';

export const AIChatPage: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'init',
      sender: 'assistant',
      text: 'Chào mừng bạn đến với Trung tâm Tư vấn AI của LaptopStore! Tôi đã được nạp dữ liệu danh mục và tình trạng tồn kho của cửa hàng. Bạn cần tìm laptop để làm việc, học tập, đồ họa hay giải trí với ngân sách bao nhiêu?',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [input, setInput] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);

  const handleSend = async (textToSend?: string) => {
    const text = (textToSend || input).trim();
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
        text: 'Không thể kết nối máy chủ AI lúc này. Vui lòng thử lại sau.',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 840, margin: '2rem auto', padding: '0 1rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 700, margin: 0, color: '#0f172a' }}>
            Trợ Lý AI Tư Vấn Laptop
          </h1>
          <p style={{ color: '#64748b', margin: '0.25rem 0 0', fontSize: '0.9375rem' }}>
            Hỏi đáp thông minh kết hợp trực tiếp dữ liệu kho hàng LaptopStore
          </p>
        </div>
        <Link to="/" style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}>
          ← Về trang chủ
        </Link>
      </div>

      <div style={{
        backgroundColor: '#ffffff',
        borderRadius: 16,
        border: '1px solid #e2e8f0',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)',
        display: 'flex',
        flexDirection: 'column',
        height: '65vh',
        overflow: 'hidden',
      }}>
        {/* Messages */}
        <div style={{
          flex: 1,
          padding: '1.5rem',
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column',
          gap: '1rem',
          backgroundColor: '#f8fafc',
        }}>
          {messages.map((m) => (
            <div
              key={m.id}
              style={{
                alignSelf: m.sender === 'user' ? 'flex-end' : 'flex-start',
                maxWidth: '75%',
              }}
            >
              <div
                style={{
                  padding: '1rem 1.25rem',
                  borderRadius: 14,
                  fontSize: '0.9375rem',
                  lineHeight: 1.5,
                  backgroundColor: m.sender === 'user' ? '#2563eb' : '#ffffff',
                  color: m.sender === 'user' ? '#ffffff' : '#1e293b',
                  border: m.sender === 'assistant' ? '1px solid #e2e8f0' : 'none',
                  borderBottomRightRadius: m.sender === 'user' ? 2 : 14,
                  borderBottomLeftRadius: m.sender === 'assistant' ? 2 : 14,
                  whiteSpace: 'pre-line',
                }}
              >
                {m.text}
              </div>
              <div style={{
                fontSize: '0.6875rem',
                color: '#94a3b8',
                marginTop: 4,
                textAlign: m.sender === 'user' ? 'right' : 'left',
              }}>
                {m.timestamp}
              </div>
            </div>
          ))}

          {loading && (
            <div style={{ alignSelf: 'flex-start', padding: '0.75rem 1rem', backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #e2e8f0', color: '#64748b', fontSize: '0.875rem' }}>
              🤖 AI đang suy nghĩ câu trả lời phù hợp nhất...
            </div>
          )}
        </div>

        {/* Input Form */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          style={{
            padding: '1rem',
            borderTop: '1px solid #e2e8f0',
            display: 'flex',
            gap: '0.75rem',
            backgroundColor: '#ffffff',
          }}
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ví dụ: Tôi có 18 triệu muốn mua laptop Asus hoặc Lenovo cấu hình tốt..."
            disabled={loading}
            style={{
              flex: 1,
              padding: '0.75rem 1rem',
              borderRadius: 8,
              border: '1px solid #cbd5e1',
              fontSize: '0.9375rem',
              outline: 'none',
            }}
          />
          <button
            type="submit"
            disabled={loading || !input.trim()}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: loading || !input.trim() ? '#93c5fd' : '#2563eb',
              color: '#ffffff',
              border: 'none',
              borderRadius: 8,
              fontWeight: 600,
              fontSize: '0.9375rem',
              cursor: loading || !input.trim() ? 'not-allowed' : 'pointer',
            }}
          >
            Gửi câu hỏi
          </button>
        </form>
      </div>
    </div>
  );
};
