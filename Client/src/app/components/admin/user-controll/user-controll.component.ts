import { Component, OnInit } from '@angular/core';
import { AdminUserInfo } from '../../../models/admin-user';
import { AdminService } from '../../../services/admin.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserInfo } from '../../../models/user';
import { AuthService } from '../../../services/auth.service';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-user-controll',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, FormsModule],
  templateUrl: './user-controll.component.html',
  styleUrl: './user-controll.component.css',
})
export class UserControllComponent implements OnInit {
  users: AdminUserInfo[] = [];
  currentPage = 0;
  pageSize = 10;
  totalUsers = 0;
  isActiveUsers: boolean | undefined = undefined; // Фильтр по активности
  searchId: number | null = null; // Фильтр по ID
  sortBy: 'id' | 'updatedAt' | 'isActive' | null = null; // Сортировка
  sortDirection: 'asc' | 'desc' = 'asc'; // Направление сортировки

  constructor(private adminService: AdminService, private userService:UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    const params:any = {
      page: this.currentPage,
      size: this.pageSize,
      isActive: this.isActiveUsers,
      id: this.searchId,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection,
    };

    this.adminService.getUsers(params).subscribe({
      next: (response: any) => {
        this.users = response.content;
        this.totalUsers = response.totalElements;
      },
      error: (err: Error) => console.error('Ошибка при загрузке пользователей:', err),
    });
  }

  blockUser(userId: number): void {
    this.adminService.blockUser(userId).subscribe({
      next: () => {
        this.loadUsers(); // Обновляем список после блокировки
      },
      error: (err: Error) => console.error('Ошибка при блокировке пользователя:', err),
    });
  }

  unblockUser(userId: number): void {
    this.adminService.unblockUser(userId).subscribe({
      next: () => {
        this.loadUsers(); // Обновляем список после разблокировки
      },
      error: (err: Error) => console.error('Ошибка при разблокировке пользователя:', err),
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUsers();
  }

  onFilterChange(): void {
    this.currentPage = 1; // Сбрасываем страницу на первую при изменении фильтров
    this.loadUsers();
  }

  onSort(column: 'id' | 'updatedAt' | 'isActive'): void {
    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc'; // Меняем направление сортировки
    } else {
      this.sortBy = column;
      this.sortDirection = 'asc'; // Устанавливаем сортировку по умолчанию
    }
    this.loadUsers();
  }

  getPages(): number[] {
    const totalPages = Math.ceil(this.totalUsers / this.pageSize); // Общее количество страниц
    const visiblePages = 5; // Количество видимых страниц в пагинации

    let startPage = Math.max(0, this.currentPage - Math.floor(visiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + visiblePages - 1);

    // Корректируем startPage, если endPage достиг предела
    if (endPage === totalPages - 1) {
      startPage = Math.max(0, endPage - visiblePages + 1);
    }

    // Генерируем массив страниц
    return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
  }
}