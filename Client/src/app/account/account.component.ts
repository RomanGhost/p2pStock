import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // Импортируем CommonModule
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css'],
  standalone: true, // Указываем, что компонент standalone
  imports: [CommonModule] // Добавляем CommonModule сюда
})
export class AccountComponent {

  constructor(private authService: AuthService) {}

  // Получаем информацию о пользователе из токена, если он есть
  get user() {
    const token = this.authService.getToken();
    if (token) {
      // В реальном приложении сюда можно добавить логику для декодирования JWT,
      // чтобы получить данные пользователя
      return { username: 'John Doe', email: 'johndoe@example.com' }; // Пример
    }
    return null;
  }

  logout() {
    this.authService.logout(); // Выход из системы
  }
}
