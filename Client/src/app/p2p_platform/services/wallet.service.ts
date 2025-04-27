import { Injectable } from '@angular/core';
import { API_CONFIG } from '../../configs/api-config';
import { AuthService } from './auth.service';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Wallet } from '../models/wallet';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private apiUrl = `${API_CONFIG.apiUrl}/wallet`;

  constructor(private http: HttpClient, private authService: AuthService) {}
  
  getUserWallets(cryptocurrencyCode: string=""): Observable<Wallet[]> {
    const params: any = {
      cryptocurrencyCode
    };

    return this.http.get<Wallet[]>(`${this.apiUrl}/get_user_wallets`, {
      headers: this.authService.getHeaders(),
      params,
    });
  }

  addNewWallet(wallet: Wallet): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.apiUrl}/add`, wallet, {
      headers: this.authService.getHeaders()
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          return throwError(() => new Error('Неверный логин или пароль'));
        } else if (error.status === 409) {
          // Сообщение об ошибке при конфликте (дубликат карты)
          return throwError(() => new Error('Кошелек c таким именем уже существует'));
        }
        return throwError(() => new Error('Произошла ошибка при добавлении кошелек'));
      })
    );
  }  

  deleteWallet(walletId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete?walletId=${walletId}`, {
      headers: this.authService.getHeaders()
    }).pipe(
      catchError((error: HttpErrorResponse) => {
        return throwError(() => new Error('Ошибка при удалении кошелька'));
      })
    );
  }
}
