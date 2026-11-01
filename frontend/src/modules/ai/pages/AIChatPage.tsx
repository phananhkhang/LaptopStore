import { Icon, PageHeading } from '@/components/ui';
import React, { useEffect, useRef, useState } from 'react';
import { useAIChat } from '../hooks/useAIChat';

const SUGGESTIONS = [
  'Mình cần laptop văn phòng mỏng nhẹ dưới 20 triệu, pin tốt.',
  'Sinh viên IT học lập trình, ngân sách khoảng 25 triệu thì nên chọn máy nào?',
  'Laptop gaming RTX 4060, RAM 16GB trở lên, dưới 40 triệu.',
  'Máy cho thiết kế đồ họa, màn hình chuẩn màu.',
];

export const AIChatPage: React.FC = () => {
  const { messages, loading, send, reset } = useAIChat(
    'Chào bạn! Mình là trợ lý tư vấn của LaptopStore. Hãy kể nhu cầu (học tập, làm việc, đồ họa, chơi game) và ngân sách, mình sẽ gợi ý chiếc máy phù hợp nhé.'
  );
  const [input, setInput] = useState('');
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' });
  }, [messages, loading]);

  const submit = (text: string) => {
    void send(text);
    setInput('');
  };

  return (
    <div className="ls-container section">
      <PageHeading
        eyebrow="Tư vấn cùng AI"
        title="Kể nhu cầu, nhận gợi ý ngay."
        sub="Trợ lý tham khảo danh mục và tồn kho hiện có của cửa hàng."
        actions={<button type="button" className="btn sm" onClick={reset}><Icon name="refresh" /> Cuộc trò chuyện mới</button>}
      />
      <div className="chat-layout">
        <aside className="panel chat-suggestions">
          <h3>Gợi ý câu hỏi</h3>
          {SUGGESTIONS.map((s) => (
            <button key={s} type="button" disabled={loading} onClick={() => submit(s)}>{s}</button>
          ))}
        </aside>
        <div className="chat-box">
          <div className="chat-messages" ref={listRef} aria-live="polite">
            {messages.map((m) => (
              <div key={m.id} className={`chat-message ${m.sender === 'user' ? 'user' : ''}`}>
                {m.text}
                <time>{m.timestamp}</time>
              </div>
            ))}
            {loading && <div className="chat-message"><span className="typing"><span /><span /><span /></span></div>}
          </div>
          <form className="chat-compose" onSubmit={(e) => { e.preventDefault(); submit(input); }}>
            <input
              className="input"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ví dụ: Tôi có 18 triệu, muốn mua laptop ASUS hoặc Lenovo cho học tập..."
              aria-label="Câu hỏi cho trợ lý AI"
              disabled={loading}
            />
            <button type="submit" className="btn primary" disabled={loading || !input.trim()}>
              <Icon name="send" /> Gửi
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
