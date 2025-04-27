import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { RegisterUser } from '../models/register-user';
import { LoginUser } from '../models/login-user';
import { API_CONFIG } from '../../configs/api-config';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${API_CONFIG.apiUrl}/auth`;
  private loggedIn = new BehaviorSubject<boolean>(this.checkInitialLoginStatus());
  isLoggedIn$ = this.loggedIn.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Регистрирует нового пользователя.
   * @param user - Данные для регистрации.
   * @returns Observable<any>
   */
  register(user: RegisterUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user).pipe(
      catchError((error) => {
        return throwError('Ошибка при регистрации');
      })
    );
  }

  /**
   * Выполняет вход пользователя.
   * @param user - Данные для входа.
   * @returns Observable<any>
   */
  login(user: LoginUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user).pipe(
      catchError((error) => {
        if (error.status === 401) {
          return throwError('Неверный логин или пароль');
        }
        return throwError('Ошибка при авторизации');
      }),
      tap((response: any) => {
        if (response?.token) {
          this.saveToken(response.token); // Сохраняем токен
          this.loggedIn.next(true); // Обновляем статус входа
        }
      })
    );
  }

  /**
   * Сохраняет токен в localStorage.
   * @param token - JWT токен.
   */
  saveToken(token: string): void {
    localStorage.setItem('jwt_token', token);
  }

  /**
   * Возвращает токен из localStorage.
   * @returns string | null
   */
  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  /**
   * Проверяет, авторизован ли пользователь.
   * @returns boolean
   */
  isLoggedIn(): boolean {
    return this.loggedIn.value;
  }

  /**
   * Выполняет выход пользователя.
   */
  logout(): void {
    localStorage.clear();
    this.loggedIn.next(false);
  }

  /**
   * Возвращает заголовки с авторизационным токеном.
   * @returns HttpHeaders
   */
  getHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  /**
   * Проверяет начальный статус входа на основе токена.
   * @returns boolean
   */
  private checkInitialLoginStatus(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1])); // Декодируем payload JWT
      const expirationDate = new Date(payload.exp * 1000); // Преобразуем время истечения
      return expirationDate > new Date(); // Проверяем срок действия
    } catch (error) {
      console.error('Ошибка при проверке токена:', error);
      return false;
    }
  }
}