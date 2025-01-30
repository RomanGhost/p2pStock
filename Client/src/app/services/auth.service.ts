import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { RegisterUser } from '../models/register-user';
import { LoginUser } from '../models/login-user';
import { API_CONFIG } from '../configs/api-config';
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${API_CONFIG.apiUrl}/auth`;
  private loggedIn = new BehaviorSubject<boolean>(this.checkInitialLoginStatus());
  isLoggedIn$ = this.loggedIn.asObservable();

  constructor(private http: HttpClient) {}

  register(user: RegisterUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user).pipe(
      catchError((error) => {
        return throwError('Произошла ошибка при регистрации');
      })
    );
  }

  login(user: LoginUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user).pipe(
      catchError((error) => {
        if (error.status === 401) {
          return throwError('Неверный логин или пароль');
        }
        return throwError('Произошла ошибка при авторизации');
      }),
      tap((response: any) => {
        if (response && response.token) {
          this.saveToken(response.token); // Сохраняем токен
          this.loggedIn.next(true); // Обновляем состояние входа
        }
      })
    );
  }

  saveToken(token: string) {
    localStorage.setItem('jwt_token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isLoggedIn(): boolean {
    return this.loggedIn.value;
  }

  logout() {
    localStorage.clear();
    this.loggedIn.next(false);
  }

  getHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  private checkInitialLoginStatus(): boolean {
    const token = this.getToken();
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1])); // Декодируем payload JWT
      const expirationDate = new Date(payload.exp * 1000); // Преобразуем время истечения
      return expirationDate > new Date(); // Проверяем, не истек ли срок действия
    }
    return false;
  }
}