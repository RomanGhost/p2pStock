import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth.service';
import { RegisterUser } from '../models/register-user';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})
export class RegisterComponent {
  registerForm: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit() {
    if (this.registerForm.valid) {
        const registerData: RegisterUser = this.registerForm.value as RegisterUser;

        this.authService.register(registerData).subscribe({
            next: response => {
                console.log('User registered successfully:', response);
                // Сохраняем JWT токен
                this.authService.saveToken(response.token);
                this.router.navigate(['/account']);
            },
            error: error => {
                console.error('Registration error:', error);
            }
        });
    }
  }
}
