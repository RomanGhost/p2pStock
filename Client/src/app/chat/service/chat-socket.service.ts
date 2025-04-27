import { inject, Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Message } from '../model/message.model';
import { MessageService } from './message.service';
import { CHAT_API_CONFIG } from '../../configs/api-config';

@Injectable({
  providedIn: 'root'
})
export class ChatSocketService {
  private socketUrl = CHAT_API_CONFIG.wsUrl;
  private socket!: WebSocket;
  private messageSubject = new Subject<Message>();
  private countConnect = 0;
  private messageService = inject(MessageService);

  private getMessages():void {
    this.messageSubject = this.messageService.getMessages();
  }

  connectWebSocket(chatId: number): void {
    this.countConnect++;
    const token = localStorage.getItem('jwt_token');
    if (!token) {
      console.error('Authentication token is missing');
      return;
    }

    this.socket = new WebSocket(`${this.socketUrl}?chat_id=${chatId}`);

    this.socket.onopen = () => {
      this.socket.send(JSON.stringify({ type: "auth", token }));
      this.countConnect = 0;
      this.messageService.loadMessageHistory(chatId);
    };
    this.getMessages()

    this.socket.onmessage = (event) => {
      try {
        const message: Message = JSON.parse(event.data);
        this.messageSubject.next(message);
      } catch (error) {
        console.error('Error parsing message:', error);
      }
    };

    this.socket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    this.socket.onclose = (event) => {
      if (!event.wasClean && this.countConnect < 10) {
        setTimeout(() => this.connectWebSocket(chatId), 1000);
      }
    };
  }

  sendMessage(content: string, chatId: number, sender: string): void {
    if (!sender) {
      console.error('User login not available');
      return;
    }

    const message: Message = {
      senderUserLogin: sender,
      message: content,
      chatId,
      time: Math.floor(Date.now() / 1000),
      id: 0
    };
    this.messageSubject.next(message);

    if (this.socket?.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(message));
    } else {
      console.error('WebSocket is not connected');
    }
  }

  disconnectWebSocket(): void {
    if (this.socket) {
      this.socket.close();
      console.log('WebSocket connection closed');
    }
  }
}
