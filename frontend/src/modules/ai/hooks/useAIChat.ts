import { useCallback, useState } from 'react';
import { aiApi } from '../api/aiApi';
import { ChatMessage } from '../types';

const now = () => new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });

/** Trạng thái hội thoại tư vấn AI dùng chung cho trang /ai-assistant và widget nổi. */
export const useAIChat = (welcome: string) => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    { id: 'welcome', sender: 'assistant', text: welcome, timestamp: now() },
  ]);
  const [loading, setLoading] = useState(false);

  const send = useCallback(async (raw: string) => {
    const text = raw.trim();
    if (!text || loading) return;
    setMessages((prev) => [...prev, { id: `user-${Date.now()}`, sender: 'user', text, timestamp: now() }]);
    setLoading(true);
    try {
      const response = await aiApi.chat({ prompt: text });
      setMessages((prev) => [...prev, { id: `ai-${Date.now()}`, sender: 'assistant', text: response.reply, timestamp: now() }]);
    } catch {
      setMessages((prev) => [...prev, {
        id: `err-${Date.now()}`,
        sender: 'assistant',
        text: 'Không thể kết nối máy chủ AI lúc này. Bạn vui lòng thử lại sau ít phút nhé.',
        timestamp: now(),
      }]);
    } finally {
      setLoading(false);
    }
  }, [loading]);

  const reset = useCallback(() => {
    setMessages((prev) => prev.slice(0, 1));
  }, []);

  return { messages, loading, send, reset };
};
