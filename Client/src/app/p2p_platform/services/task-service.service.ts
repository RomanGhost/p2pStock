import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../configs/api-config';
import { TaskInfo } from '../models/task';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = `${API_CONFIG.apiUrl}/moderation/task`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  /**
   * Получить все актуальные задачи.
   * @returns Observable со списком задач.
   */
  getAllActualTasks(): Observable<TaskInfo[]> {
    return this.http.get<TaskInfo[]>(`${this.apiUrl}/get/all/actual`, {
      headers: this.authService.getHeaders()
    });
  }

  /**
   * Взять задачу в работу.
   * @param taskId ID задачи.
   * @returns Observable с обновленной задачей.
   */
  takeInWork(taskId: number): Observable<TaskInfo> {
    return this.http.patch<TaskInfo>(`${this.apiUrl}/take_in_word/${taskId}`, {}, {
      headers: this.authService.getHeaders()
    });
  }

  /**
   * Принять задачу.
   * @param taskId ID задачи.
   * @param result Результат выполнения задачи.
   * @returns Observable с обновленной задачей.
   */
  acceptTask(taskId: number, result: string): Observable<TaskInfo> {
    return this.http.patch<TaskInfo>(`${this.apiUrl}/accept/${taskId}`, { result }, {
      headers: this.authService.getHeaders()
    });
  }

  /**
   * Отклонить задачу.
   * @param taskId ID задачи.
   * @param result Описание причины отклонения.
   * @returns Observable с обновленной задачей.
   */
  denyTask(taskId: number, result: string): Observable<TaskInfo> {
    return this.http.patch<TaskInfo>(`${this.apiUrl}/deny/${taskId}`, { result }, {
      headers: this.authService.getHeaders()
    });
  }
}
