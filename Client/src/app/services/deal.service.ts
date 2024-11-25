import { Injectable } from '@angular/core';
import { API_CONFIG } from '../configs/api-config';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './auth.service';
import { catchError, Observable, throwError } from 'rxjs';
import { CreateDealInfo, DealInfo } from '../models/deal';
import { PaginationResponse } from '../models/pagination';

@Injectable({
  providedIn: 'root'
})
export class DealService {
  private apiUrl = `${API_CONFIG.apiUrl}/order`;

  constructor(private http: HttpClient, private authService: AuthService) { }
  /**
     * Получение всех сделок
     */
  getAllDeals(): Observable<DealInfo[]> {
    return this.http.get<DealInfo[]>(`${this.apiUrl}/get/all`, {
      headers: this.authService.getHeaders(),
    }).pipe(catchError(this.handleError));
  }

  /**
   * Получение сделки по ID
   * @param dealId ID сделки
   */
  getDealById(dealId: number): Observable<DealInfo> {
    return this.http.get<DealInfo>(`${this.apiUrl}/get/${dealId}`, {
      headers: this.authService.getHeaders(),
    }).pipe(catchError(this.handleError));
  }

  /**
   * Получение сделок с фильтрацией и пагинацией
   * @param statusName Статус сделки (опционально)
   * @param changedAfter Дата изменения статуса (опционально)
   * @param sortOrder Порядок сортировки по дате (по умолчанию 'asc')
   * @param page Номер страницы (опционально)
   * @param size Количество элементов на странице (опционально)
   */
  getFilteredDeals(
    statusName?: string,
    changedAfter?: string,
    sortOrder: string = 'asc',
    page: number = 0,
    size: number = 10
  ): Observable<PaginationResponse<DealInfo>> {
    // Создаём объект с параметрами запроса
    const params: any = {
      statusName,
      changedAfter,
      sortOrder,
      page,
      size,
    };

    // Убираем undefined, null и пустые строки из параметров
    Object.keys(params).forEach((key) => {
      if (params[key] === '' || params[key] == null) {
        delete params[key];
      }
    });

    return this.http.get<PaginationResponse<DealInfo>>(`${this.apiUrl}/get/filter`, {
      headers: this.authService.getHeaders(),
      params,
    }).pipe(catchError(this.handleError));
  }

  /**
   * Добавление новой сделки
   * @param deal Новый объект сделки
   */
  addDeal(deal: CreateDealInfo): Observable<DealInfo> {
    return this.http.post<DealInfo>(`${this.apiUrl}/add`, deal, {
      headers: this.authService.getHeaders(),
    }).pipe(catchError(this.handleError));
  }

  /**
   * Обработчик ошибок
   */
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
