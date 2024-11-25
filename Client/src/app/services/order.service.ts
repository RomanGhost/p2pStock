import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { API_CONFIG } from '../configs/api-config';
import { AuthService } from './auth.service';
import { CreateOrderInfo, OrderInfo } from '../models/order';
import { PaginationResponse } from '../models/pagination';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private apiUrl = `${API_CONFIG.apiUrl}/order`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  /**
   * Получение всех заказов
   */
  getAllOrders(): Observable<OrderInfo[]> {
    return this.http.get<OrderInfo[]>(`${this.apiUrl}/get/all`, {
      headers: this.authService.getHeaders(),
    }).pipe(catchError(this.handleError));
  }

  /**
   * Получение заказа по ID
   * @param orderId ID заказа
   */
  getOrderById(orderId: number): Observable<OrderInfo> {
    return this.http.get<OrderInfo>(`${this.apiUrl}/get/${orderId}`, {
      headers: this.authService.getHeaders(),
    }).pipe(catchError(this.handleError));
  }

  /**
   * Получение всех заказов с фильтрацией и пагинацией
   * @param status Статус заказа (опционально)
   * @param type Тип заказа (опционально)
   * @param cryptoCode Код криптовалюты (опционально)
   * @param page Номер страницы (опционально)
   * @param size Количество элементов на странице (опционально)
   * @param sortOrder Порядок сортировки по дате (по умолчанию 'asc')
   */
  getFilteredOrders(
    status?: string,
    type?: string,
    cryptoCode?: string,
    page: number = 0,
    size: number = 10,
    sortOrder: string = 'asc'
  ): Observable<PaginationResponse<OrderInfo>> {
    // Создаём объект с параметрами запроса
    const params: any = {
      status,
      type,
      cryptoCode,
      page,
      size,
      sortOrder,
    };
  
    // Убираем undefined, null и пустые строки из параметров
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] == null) {
        delete params[key];
      }
    });
  
    return this.http.get<PaginationResponse<OrderInfo>>(`${this.apiUrl}/get/filter`, {
      headers: this.authService.getHeaders(),
      params,
    }).pipe(catchError(this.handleError));
  }
  

  /**
   * Добавление нового заказа
   * @param order Новый заказ
   */
  addOrder(order: CreateOrderInfo): Observable<OrderInfo> {
    return this.http.post<OrderInfo>(`${this.apiUrl}/add`, order, {
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
