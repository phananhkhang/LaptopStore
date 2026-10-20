export interface AIChatRequest {
  prompt: string;
  systemInstruction?: string;
}

export interface AIChatResponse {
  reply: string;
  model: string;
  executionTimeMs: number;
}

export interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  timestamp: string;
}
