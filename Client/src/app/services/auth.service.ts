import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RegisterUser } from '../models/register-user';
import { LoginUser } from '../models/login-user';
import { API_CONFIG } from '../configs/api-config';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${API_CONFIG.apiUrl}/auth`; // Убедитесь, что используете правильный URL

  constructor(private http: HttpClient) {}

  register(user: RegisterUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  login(user: LoginUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user).pipe(
      catchError((error) => {
        if (error.status === 401) {
          return throwError('Неверный логин или пароль'); // Возвращаем кастомное сообщение об ошибке
        }
        return throwError('Произошла ошибка при авторизации');
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
    const token = this.getToken();
    return token !== null;
  }

  logout() {
    localStorage.removeItem('jwt_token'); // Удаляем токен при выходе
  }

  getHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }
}
