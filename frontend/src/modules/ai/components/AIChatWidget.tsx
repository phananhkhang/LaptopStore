import { Icon } from '@/components/ui';
import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAIChat } from '../hooks/useAIChat';

const QUICK_PROMPTS = [
  'Laptop văn phòng mỏng nhẹ dưới 20 triệu',
  'Laptop sinh viên pin trâu, bền bỉ',
  'Laptop gaming RAM 16GB+',
];

export const AIChatWidget: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const { messages, loading, send } = useAIChat(
    'Xin chào! Mình là trợ lý AI của LaptopStore. Bạn cần tìm laptop cho nhu cầu và tầm giá nào?'
  );
  const endRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isOpen) endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isOpen, loading]);

  const submit = (text: string) => {
    void send(text);
    setInput('');
  };

  return (
    <div className="chat-fab">
      {isOpen ? (
        <div className="chat-panel" role="dialog" aria-label="Trợ lý AI LaptopStore">
          <div className="chat-panel-head">
            <div className="ai-symbol" style={{ width: 38, height: 38, borderRadius: 12 }}><Icon name="sparkles" /></div>
            <div style={{ flex: 1 }}>
              <strong style={{ display: 'block', fontSize: 13 }}>Trợ lý AI LaptopStore</strong>
              <small className="muted">Tư vấn cấu hình & tồn kho</small>
            </div>
            <Link to="/ai-assistant" className="icon-btn" aria-label="Mở trang tư vấn" onClick={() => setIsOpen(false)}><Icon name="grid" /></Link>
            <button type="button" className="icon-btn" aria-label="Đóng trợ lý" onClick={() => setIsOpen(false)}><Icon name="close" /></button>
          </div>
          <div className="chat-quick">
            {QUICK_PROMPTS.map((q) => <button key={q} type="button" disabled={loading} onClick={() => submit(q)}>{q}</button>)}
          </div>
          <div className="chat-messages" aria-live="polite">
            {messages.map((m) => (
              <div key={m.id} className={`chat-message ${m.sender === 'user' ? 'user' : ''}`}>
                {m.text}
                <time>{m.timestamp}</time>
              </div>
            ))}
            {loading && <div className="chat-message"><span className="typing"><span /><span /><span /></span></div>}
            <div ref={endRef} />
          </div>
          <form className="chat-compose" style={{ padding: 12 }} onSubmit={(e) => { e.preventDefault(); submit(input); }}>
            <input className="input" value={input} onChange={(e) => setInput(e.target.value)} placeholder="Nhập câu hỏi tư vấn..." aria-label="Câu hỏi cho trợ lý AI" disabled={loading} />
            <button type="submit" className="btn primary" disabled={loading || !input.trim()} aria-label="Gửi"><Icon name="send" /></button>
          </form>
        </div>
      ) : (
        <button type="button" className="chat-fab-btn" onClick={() => setIsOpen(true)}>
          <Icon name="sparkles" /> Tư vấn AI
        </button>
      )}
    </div>
  );
};
