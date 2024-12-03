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
  private apiUrl = `${API_CONFIG.apiUrl}/auth`; // Убедитесь, что используете правильный URL
  private loggedIn = new BehaviorSubject<boolean>(this.checkInitialLoginStatus()); // Проверяем начальное состояние
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
      // После успешного логина обновляем состояние
      tap((response) => {
        if (response) {
          this.loggedIn.next(true); // Обновляем состояние входа
        }
      })
    );
  }

  saveToken(token: string) {
    localStorage.setItem('jwt_token', token); // Сохраняем JWT в localStorage
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token'); // Получаем токен из localStorage
  }

  isLoggedIn(): boolean {
    return this.loggedIn.value; // Используем BehaviorSubject для проверки состояния
  }

  logout() {
    localStorage.clear();  // Очистка localStorage
    this.loggedIn.next(false);  // Обновляем состояние входа
  }

  getHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  private checkInitialLoginStatus(): boolean {
    // Проверяем, есть ли токен в localStorage для установки начального состояния
    return this.getToken() !== null;
  }
}
