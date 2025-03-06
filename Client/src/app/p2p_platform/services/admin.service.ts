import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { API_CONFIG } from '../configs/api-config';
import { PaginationResponse } from '../models/pagination';
import { AdminUserInfo } from '../models/admin-user';
import { Observable } from 'rxjs/internal/Observable';
import { catchError, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${API_CONFIG.apiUrl}/user/admin`;

  constructor(private http: HttpClient, private authService: AuthService) {}
  
  getUsers(params: {
    page: number;
    size: number;
    isActive?: boolean | null;
    id?: number | null;
    sortBy?: string | null;
    sortDirection?: string | null;
  }): Observable<PaginationResponse<AdminUserInfo>> {
    let httpParams = new HttpParams()
      .set('page', params.page.toString())
      .set('size', params.size.toString());
  
    if (params.isActive !== undefined && params.isActive !== null) {
      httpParams = httpParams.set('isActive', params.isActive.toString());
    }
    if (params.id !== undefined && params.id !== null) {
      httpParams = httpParams.set('id', params.id.toString());
    }
    if (params.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params.sortDirection) {
      httpParams = httpParams.set('sortDirection', params.sortDirection);
    }
  
    return this.http.get<PaginationResponse<AdminUserInfo>>(`${this.apiUrl}/get_all`, { 
      headers: this.authService.getHeaders(),
      params: httpParams 
    });
  }
  
  

  blockUser(userId:number):Observable<AdminUserInfo>{
    return this.http.patch<AdminUserInfo>(`${this.apiUrl}/block`, {}, {
      headers: this.authService.getHeaders(),
      params: {userId}
    });
  }

  unblockUser(userId:number):Observable<AdminUserInfo>{
    return this.http.patch<AdminUserInfo>(`${this.apiUrl}/unblock`, {}, {
      headers: this.authService.getHeaders(),
      params: {userId}
    });
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
      let errorMessage = 'Произошла ошибка';
      if (error.status === 401 || error.status === 403) {
        errorMessage = 'Вы не авторизованы';
      } else if (error.status === 404) {
        errorMessage = 'Ресурс не найден';
      } else if (error.status >= 500) {
        errorMessage = 'Ошибка сервера. Попробуйте позже';
      }
      return throwError(() => new Error(errorMessage));
    }
}
