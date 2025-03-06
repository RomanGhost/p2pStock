import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Message } from '../../model/message.model';
import { ChatService } from '../../service/chat.service';
import { ChatInfo } from '../../model/chat.model';
import { MessageService } from '../../service/message.service';
import { AuthService } from '../../service/auth.service';
import { ChatSocketService } from '../../service/chat-socket.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit, OnDestroy {
  private chatService = inject(ChatService);
  private messageService = inject(MessageService);
  // private authService = inject(AuthService);
  private socketService = inject(ChatSocketService);
  currentChatUser = '';

  messages: Message[] = [];
  chats: ChatInfo[] = [];
  newMessage = '';
  currentUserLogin = '';
  currentChatId: number | null = null;
  isConnected = false;

  ngOnInit(): void {
    // this.authService.setAuthToken("eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjp7ImF1dGhvcml0eSI6IlJPTEVfTUFOQUdFUiJ9LCJzdWIiOiJyb21hbjIiLCJpYXQiOjE3NDExNzMyNzcsImV4cCI6MTc0MTI1OTY3N30.OXx9Q354E5j1-WNBdlEjjyXUO9TstpESLixai2B6CjM");
    this.currentUserLogin = localStorage.getItem('userLogin') || '';

    this.chatService.getAllChat().subscribe(chats => {
      this.chats = chats;
    });

    this.messageService.subscribeOnMessages().subscribe(msg => {
      this.messages.push(msg);
    });
  }

  createChat(): void {
    this.chatService.createChat("roman", "roman2", 0).subscribe(chat => {
      if (chat.chatId !== -1) {
        this.chats.push(chat);
        this.handleContinueChat(chat.chatId);
      }
    });
  }

  sendMessage(): void {
    if (this.newMessage.trim() && this.currentChatId) {
      this.socketService.sendMessage(this.newMessage, this.currentChatId, this.currentUserLogin);
      this.newMessage = '';
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  handleContinueChat(chatId: number): void {
    if (this.currentChatId !== chatId) {
        this.socketService.disconnectWebSocket();
        this.messages = []; // Очистка сообщений
        this.currentChatId = chatId;
        
        // Определяем имя собеседника
        const chat = this.chats.find(c => c.chatId === chatId);
        this.currentChatUser = chat ? (this.currentUserLogin === chat.firstLogin ? chat.secondLogin : chat.firstLogin) : 'Неизвестный';

        this.socketService.connectWebSocket(chatId);
        this.isConnected = true;
    }
}


  disconnect(): void {
    this.socketService.disconnectWebSocket();
    this.currentChatId = -1;
    this.isConnected = false;
  }
}
