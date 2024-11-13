// src/app/services/data.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../configs/api-config';
import { Data } from '../models/data';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  private apiUrl = `${API_CONFIG.apiUrl}/data`; // URL API

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // Получение всех банков
  getBanks(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/all_banks`, {
      headers: this.getHeaders()
    });
  }

  // Получение всех криптовалют
  getCryptocurrencies(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/all_crypto`, {
      headers: this.getHeaders()
    });
  }

  // Получение всех статусов сделок
  getDealStatuses(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/all_deal_statuses`, {
      headers: this.getHeaders()
    });
  }

  // Получение всех типов заказов
  getOrderTypes(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/all_order_types`, {
      headers: this.getHeaders()
    });
  }

  // Получение всех статусов заказов
  getOrderStatuses(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/all_order_statuses`, {
      headers: this.getHeaders()
    });
  }

  // Получение всех приоритетов
  getPriorities(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/all_priorities`, {
      headers: this.getHeaders()
    });
  }

  // Получение всех ролей
  getRoles(): Observable<Data[]> {
    return this.http.get<Data[]>(`${this.apiUrl}/all_roles`, {
      headers: this.getHeaders()
    });
  }
}
