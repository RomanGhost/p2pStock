import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../configs/api-config';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private apiUrl = `${API_CONFIG.apiUrl}/platform/order`;

  constructor(private http: HttpClient) {}

  saveOrder(orderData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/save`, orderData);
  }
}
