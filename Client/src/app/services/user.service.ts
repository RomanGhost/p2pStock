import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { API_CONFIG } from '../configs/api-config';
import { UserInfo } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${API_CONFIG.apiUrl}/user`;
  private cachedUser: UserInfo | undefined = undefined;

  constructor(private http: HttpClient, private authService: AuthService) {}

  getUserProfile(): Observable<UserInfo> {
    if (this.cachedUser) {
      return new Observable(observer => {
        observer.next(this.cachedUser);
        observer.complete();
      });
    }

    const token = this.authService.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    // Запрашиваем данные пользователя по username
    return this.http.get<UserInfo>(`${this.apiUrl}/profile`, {
      headers
    });
  }

  cacheUser(userInfo: UserInfo): void {
    this.cachedUser = userInfo;
  }

  clearCache(): void {
    this.cachedUser = undefined;
  }
}
