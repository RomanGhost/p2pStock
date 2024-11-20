import { Injectable } from '@angular/core';
import { API_CONFIG } from '../configs/api-config';
import { AuthService } from './auth.service';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Card } from '../models/card';

@Injectable({
  providedIn: 'root'
})
export class CardService {
  private apiUrl = `${API_CONFIG.apiUrl}/card`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  getUserCards(): Observable<Card[]> {
    return this.http.get<Card[]>(`${this.apiUrl}/get_user_cards`, {
      headers: this.authService.getHeaders()
    });
  }

  addNewCard(card: Card): Observable<Card> {
    return this.http.post<Card>(`${this.apiUrl}/add`, card, {
      headers: this.authService.getHeaders()
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          return throwError(() => new Error('Неверный логин или пароль'));
        } else if (error.status === 409) {
          // Сообщение об ошибке при конфликте (дубликат карты)
          return throwError(() => new Error('Карта с таким номером или именем уже существует'));
        }
        return throwError(() => new Error('Произошла ошибка при добавлении карты'));
      })
    );
  }  

  deleteCard(cardId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete?cardId=${cardId}`, {
      headers: this.authService.getHeaders()
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        return throwError(() => new Error('Ошибка при удалении карты'));
      })
    );
  }
}
