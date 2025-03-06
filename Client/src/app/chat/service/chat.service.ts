import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatInfo, ChatList } from '../model/chat.model';
import { AuthService } from './auth.service';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:8081/api/v1/chat';

  createChat(firstLogin: string, secondLogin: string, dealId: number): Observable<ChatInfo> {
    const request: ChatInfo = {
      chatId: -1,
      firstLogin,
      secondLogin,
      dealId
    };

    return this.http.post<ChatInfo>(`${this.apiUrl}/create`, request, { headers: this.authService.getAuthHeaders() })
      .pipe(
        catchError(err => {
          console.error('Chat creation failed:', err);
          return of({ chatId: -1, firstLogin: '', secondLogin: '', dealId: 0 });
        })
      );
  }

  getAllChat(): Observable<ChatInfo[]> {
    return this.http.get<ChatList>(`${this.apiUrl}/get`, { headers: this.authService.getAuthHeaders() })
      .pipe(
        map(response => response.chatList),
        catchError(err => {
          console.error('Error fetching chats:', err);
          return of([]);
        })
      );
  }
}
