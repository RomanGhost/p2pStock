import { Injectable } from '@angular/core';
import { API_CONFIG } from '../configs/api-config';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private wsUrl = `${API_CONFIG.wsUrl}/deal`;
  socket: WebSocket | null = null;

  connect(): void {
    // Проверяем, если WebSocket уже подключён, не подключаем повторно
    if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
      console.log('WebSocket уже подключен или подключается');
      return;
    }

    this.socket = new WebSocket(this.wsUrl);

    this.socket.onopen = () => {
      console.log('WebSocket подключен:', this.wsUrl);
    };

    this.socket.onmessage = (event) => {
      const deal = JSON.parse(event.data); // Преобразуем JSON в объект
      console.log('Обновление сделки:', deal);
    };

    this.socket.onclose = () => {
      console.log('WebSocket закрыт. Переподключение через 5 секунд...');
      setTimeout(() => this.connect(), 5000);
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
