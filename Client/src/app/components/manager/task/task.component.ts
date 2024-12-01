import { Component, OnInit } from '@angular/core';
import { TaskInfo } from '../../../models/tasl';
import { TaskService } from '../../../task-service.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-task',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './task.component.html',
  styleUrl: './task.component.css'
})

export class TaskComponent implements OnInit {
  tasks: TaskInfo[] = [];
  loading: boolean = true;

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.taskService.getAllActualTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.loading = false;
      },
      error: (err) => {
        console.error('Ошибка при загрузке задач:', err);
        this.loading = false;
      }
    });
  }

  takeTaskInWork(taskId: number): void {
    this.taskService.takeInWork(taskId).subscribe({
      next: (task) => {
        console.log('Задача взята в работу:', task);
        this.loadTasks(); // Обновляем список задач
      },
      error: (err) => console.error('Ошибка при взятии задачи в работу:', err)
    });
  }

  acceptTask(taskId: number, result: string): void {
    this.taskService.acceptTask(taskId, result).subscribe({
      next: (task) => {
        console.log('Задача принята:', task);
        this.loadTasks(); // Обновляем список задач
      },
      error: (err) => console.error('Ошибка при принятии задачи:', err)
    });
  }

  denyTask(taskId: number, result: string): void {
    this.taskService.denyTask(taskId, result).subscribe({
      next: (task) => {
        console.log('Задача отклонена:', task);
        this.loadTasks(); // Обновляем список задач
      },
      error: (err) => console.error('Ошибка при отклонении задачи:', err)
    });
  }
}
