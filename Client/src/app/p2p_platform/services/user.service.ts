import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { API_CONFIG } from '../configs/api-config';
import { UserInfo } from '../models/user';

const USER_CACHE_KEY = 'userCache';
export interface UserData {
  id: number;
  login: string;
  roleName: string;
}
@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${API_CONFIG.apiUrl}/user`;
  private cachedUser: UserInfo | undefined = undefined;

  constructor(
    private http: HttpClient, 
    private authService: AuthService
  ) {
    // Инициализация кеша при создании сервиса
    const cachedData = localStorage.getItem(USER_CACHE_KEY);
    this.cachedUser = cachedData ? JSON.parse(cachedData) : undefined;
  }

  getUserProfile(forceRefresh: boolean = false): Observable<UserInfo|undefined> {
    // Возвращаем кешированные данные если они есть и не требуется обновление
    if (!forceRefresh && this.cachedUser) {
      return of(this.cachedUser);
    }

    // Проверка локального хранилища
    const cachedData = localStorage.getItem(USER_CACHE_KEY);
    if (!forceRefresh && cachedData) {
      this.cachedUser = JSON.parse(cachedData);
      return of(this.cachedUser);
    }

    // Запрос новых данных
    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    return this.http.get<UserInfo>(`${this.apiUrl}/profile`, { headers }).pipe(
      tap(user => this.cacheUser(user)),
      catchError(error => {
        console.error('Ошибка получения профиля:', error);
        if (error.status === 401) {
          this.authService.logout();
        }
        return throwError(() => error.status === 401 
          ? new Error('Требуется авторизация') 
          : new Error('Ошибка сервера'));
      })
    );
  }

  cacheUser(userInfo: UserInfo): void {
    this.cachedUser = userInfo;    
    localStorage.setItem(USER_CACHE_KEY, JSON.stringify(userInfo));
    localStorage.setItem(`${USER_CACHE_KEY}Login`, userInfo.login);
  }

  clearCache(): void {
    this.cachedUser = undefined;
    localStorage.removeItem(USER_CACHE_KEY);
  }

  hasRole(roleName: string): boolean {
    if (!this.cachedUser) {
      this.getUserProfile(true).subscribe(user=>
        user?.roleName.toLowerCase() === roleName.toLowerCase().trim()
      ); // Инициируем загрузку если нет данных
      return false;
    }
    return this.cachedUser.roleName.toLowerCase() === roleName.toLowerCase().trim();
  }

  logout(): void {
    this.clearCache();
    this.authService.logout();
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
}