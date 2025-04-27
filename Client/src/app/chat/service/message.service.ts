import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Message, MessageList } from '../model/message.model';
import { AuthService } from './auth.service';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { CHAT_API_CONFIG } from '../../configs/api-config';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private messageSubject = new Subject<Message>();
  private apiUrl = `${CHAT_API_CONFIG.apiUrl}/message`;

  subscribeOnMessages(): Observable<Message> {
    return this.messageSubject.asObservable();
  }

  getMessages(): Subject<Message> {
    return this.messageSubject;
  }

  loadMessageHistory(chatId: number): void {
    this.http.get<MessageList>(`${this.apiUrl}/get/all?chat_id=${chatId}`, { 
      headers: this.authService.getAuthHeaders() 
    })
    .pipe(
      catchError(err => {
        console.error('Error loading message history:', err);
        return of({ messages: [] }); // Возвращаем пустой массив вместо `null`
      })
    )
    .subscribe(response => {
      if (response && response.messages) { // ✅ Проверка на `null`
        response.messages.forEach(msg => {
          const message: Message = { ...msg, id: 0 };
          this.messageSubject.next(message);
        });
      } else {
        console.warn('No messages received from server');
      }
    });
  }
}
