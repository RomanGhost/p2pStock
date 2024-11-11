// Пример для src/app/login/login.component.ts
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink, RouterOutlet } from '@angular/router';
import { LoginUser } from '../models/login-user';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink, RouterOutlet]
})
export class LoginComponent {
  loginForm: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
        const loginData: LoginUser = this.loginForm.value as LoginUser;

        this.authService.login(loginData).subscribe({
            next: response => {
                console.log('Login successful');
                // Сохраняем JWT токен
                this.authService.saveToken(response.token);
            },
            error: error => {
                console.error('Login error:', error);

            }
        });
    }
  }
}
