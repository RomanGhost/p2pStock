import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Message } from '../../model/message.model';
import { ChatService } from '../../service/chat.service';
import { ChatInfo } from '../../model/chat.model';
import { MessageService } from '../../service/message.service';
import { AuthService } from '../../service/auth.service';
import { ChatSocketService } from '../../service/chat-socket.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  private activatedRoute = inject(ActivatedRoute);

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
    this.currentUserLogin = localStorage.getItem('userCacheLogin') || '';

    this.chatService.getAllChat().subscribe(chats => {
      this.chats = chats;

      const queryParams = this.activatedRoute.snapshot.queryParams;
      const user = queryParams['user'];
      const dealId = queryParams['dealId'] ? +queryParams['dealId'] : null;
      console.log(`Query Params: user=${user}, dealId=${dealId}`);
      if (user && dealId) {
        this.startChat(user, dealId);
      }
    });

    this.messageService.subscribeOnMessages().subscribe(msg => {
      this.messages.push(msg);
    });
    
  }

  createChat(otherUserLogin:string, dealId:number): void {
    this.chatService.createChat(this.currentUserLogin, otherUserLogin, dealId).subscribe(chat => {
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

  startChat(userLogin:string, dealID:number){
    const res = this.chats.find((chat) => chat.dealId === dealID)
    if (res !== undefined){
      this.handleContinueChat(res.chatId);
    }else{
      this.createChat(userLogin, dealID);
    }
  }

  disconnect(): void {
    this.socketService.disconnectWebSocket();
    this.currentChatId = -1;
    this.isConnected = false;
  }
}
