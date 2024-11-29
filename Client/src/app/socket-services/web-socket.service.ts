import { Injectable } from '@angular/core';
import { API_CONFIG } from '../configs/api-config';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private wsUrl = `${API_CONFIG.wsUrl}`;
  socket: WebSocket | null = null;

  connect(url: string): void {
    // Проверяем, если WebSocket уже подключён, не подключаем повторно
    if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
      console.log('WebSocket уже подключен или подключается');
      return;
    }
    const resultUrl = `${this.wsUrl}/${url}`
    this.socket = new WebSocket(resultUrl);

    this.socket.onopen = () => {
      console.log('WebSocket подключен:', resultUrl);
    };

    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data); // Преобразуем JSON в объект
      console.log('Обновление сделки:', data);
    };

    this.socket.onclose = () => {
      console.log('WebSocket закрыт. Переподключение через 5 секунд...');
      setTimeout(() => this.connect(url), 5000);
    };

    this.socket.onerror = (error) => {
      console.error('Ошибка WebSocket:', error);
    };
  }

  disconnect(): void {
    this.socket?.close();
    this.socket = null;
  }

  subscribeToMessages(callback: (data: any) => void): void {
    this.socket?.addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      callback(data);
    });
  }
}
