import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { API_CONFIG } from '../configs/api-config';
import { UserInfo } from '../models/user';

const USER_CACHE_KEY = 'userCache';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${API_CONFIG.apiUrl}/user`;
  private cachedUser: UserInfo | undefined = undefined;

  constructor(private http: HttpClient, private authService: AuthService) {}

  getUserProfile(forceRefresh: boolean = false): Observable<UserInfo> {
    if (!forceRefresh && this.cachedUser) {
      return new Observable(observer => {
        observer.next(this.cachedUser);
        observer.complete();
      });
    }

    const cachedData = localStorage.getItem(USER_CACHE_KEY);
    if (!forceRefresh && cachedData) {
      this.cachedUser = JSON.parse(cachedData);
      return new Observable(observer => {
        observer.next(this.cachedUser);
        observer.complete();
      });
    }

    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    return this.http.get<UserInfo>(`${this.apiUrl}/profile`, { headers }).pipe(
      tap(user => {
        this.cachedUser = user;
        localStorage.setItem(USER_CACHE_KEY, JSON.stringify(user)); // Обновляем кеш
      }),
      catchError(error => {
        console.error('Error fetching user profile:', error);
        this.authService.logout();
        return throwError(() => new Error('Unauthorized'));
      })
    );
  }

  cacheUser(userInfo: UserInfo): void {
    this.cachedUser = userInfo;
    localStorage.setItem(USER_CACHE_KEY, JSON.stringify(userInfo));
  }

  clearCache(): void {
    this.cachedUser = undefined;
    localStorage.removeItem(USER_CACHE_KEY);
  }

  getCachedUser(): UserInfo | undefined {
    return this.cachedUser;
  }

  hasRole(roleName:String){
    return this.cachedUser?.roleName === roleName
  }
}
