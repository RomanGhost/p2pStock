import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegisterUser } from './models/register-user';
import { LoginUser } from './models/login-user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/v1/p2pstock/auth'; // Убедитесь, что используете правильный URL

  constructor(private http: HttpClient) {}

  register(user: RegisterUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  login(user: LoginUser): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user);
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
}
