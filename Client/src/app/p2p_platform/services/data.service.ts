// src/app/services/data.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../configs/api-config';
import { Data } from '../models/data';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  private apiUrl = `${API_CONFIG.apiUrl}/data`; // URL API

  constructor(private http: HttpClient, private authService: AuthService) {}

  // Получение всех банков
  getBanks(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/get/all/banks`, {
      headers: this.authService.getHeaders()
    });
  }

  // Получение всех криптовалют
  getCryptocurrencies(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/get/all/crypto`, {
      headers: this.authService.getHeaders()
    });
  }

  // Получение всех статусов сделок
  getDealStatuses(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/get/all/deal_statuses`, {
      headers: this.authService.getHeaders()
    });
  }

  // Получение всех типов заказов
  getOrderTypes(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/get/all/order_types`, {
      headers: this.authService.getHeaders()
    });
  }

  // Получение всех статусов заказов
  getOrderStatuses(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/get/all/order_statuses`, {
      headers: this.authService.getHeaders()
    });
  }

  // Получение всех приоритетов
  getPriorities(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/get/all/priorities`, {
      headers: this.authService.getHeaders()
    });
  }

  // Получение всех ролей
  getRoles(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/get/all/roles`, {
      headers: this.authService.getHeaders()
    });
  }
}
