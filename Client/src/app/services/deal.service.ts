import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { API_CONFIG } from '../configs/api-config';
import { AuthService } from './auth.service';
import { catchError, Observable, throwError } from 'rxjs';
import { CreateDealInfo, DealInfo } from '../models/deal';
import { PaginationResponse } from '../models/pagination';

@Injectable({
  providedIn: 'root',
})
export class DealService {
  private apiUrl = `${API_CONFIG.apiUrl}/deal`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  /**
   * Получение всех сделок
   */
  getAllDeals(): Observable<DealInfo[]> {
    return this.http
      .get<DealInfo[]>(`${this.apiUrl}/get/all`, {
        headers: this.authService.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Получение сделки по ID
   * @param dealId ID сделки
   */
  getDealById(dealId: number): Observable<DealInfo> {
    return this.http
      .get<DealInfo>(`${this.apiUrl}/get/${dealId}`, {
        headers: this.authService.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Получение сделок с фильтрацией и пагинацией
   * @param filters Фильтры и параметры пагинации
   */
  getFilteredDeals(params: {
    statusName?: string;
    changedAfter?: string;
    sortOrder?: string;
    page?: number;
    size?: number;
  }): Observable<PaginationResponse<DealInfo>> {
    const cleanParams = this.cleanParams(params);
    return this.http
      .get<PaginationResponse<DealInfo>>(`${this.apiUrl}/get/filter`, {
        headers: this.authService.getHeaders(),
        params: cleanParams,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Добавление новой сделки
   * @param deal Новый объект сделки
   */
  addDeal(deal: CreateDealInfo): Observable<DealInfo> {
    return this.http
      .post<DealInfo>(`${this.apiUrl}/add`, deal, {
        headers: this.authService.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Подтверждение следующего статуса сделки
   * @param dealId ID сделки
   */
  nextConfirm(dealId: number): Observable<DealInfo> {
    return this.http
      .patch<DealInfo>(`${this.apiUrl}/status/confirm/next/${dealId}`, null, {
        headers: this.authService.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Отклонение следующего статуса сделки
   * @param dealId ID сделки
   */
  nextReject(dealId: number): Observable<DealInfo> {
    return this.http
      .patch<DealInfo>(`${this.apiUrl}/status/reject/next/${dealId}`, null, {
        headers: this.authService.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Обработчик ошибок
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    const errorMessage = this.getErrorMessage(error);
    console.error(`DealService Error: ${errorMessage}`, error);
    return throwError(() => new Error(errorMessage));
  }

  /**
   * Возвращает сообщение об ошибке на основе статуса
   */
  private getErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 401 || error.status === 403) {
      return 'Вы не авторизованы';
    }
    if (error.status === 404) {
      return 'Ресурс не найден';
    }
    if (error.status >= 500) {
      return 'Ошибка сервера. Попробуйте позже';
    }
    return error.error?.message || 'Произошла неизвестная ошибка';
  }

  /**
   * Удаляет пустые, null и undefined параметры из объекта
   */
  private cleanParams(params: { [key: string]: any }): { [key: string]: any } {
    return Object.keys(params).reduce((acc, key) => {
      if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
        acc[key] = params[key];
      }
      return acc;
    }, {} as { [key: string]: any });
  }
}
