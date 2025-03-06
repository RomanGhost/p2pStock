import { Component, CUSTOM_ELEMENTS_SCHEMA, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink]
})
export class SidebarComponent implements OnInit {
  isLoggedIn = false;
  isManager = false;
  isAdmin = false;
  isFold=false;
  @Output() toggleMenu = new EventEmitter<boolean>();
  private router: Router = inject(Router);

  constructor(private userService: UserService, private authService: AuthService) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    
    this.authService.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status; 
      this.userService.getUserProfile().subscribe(user => {
        console.log(user);
        this.checkAccess();
      });
    });
  }

  logout(): void {
    this.userService.logout();
    this.router.navigate(['/login']).then(() => {
      this.checkAccess(); // Обновление доступа уже после перехода на страницу логина
    });
  }

  switchView(){
    this.isFold = !this.isFold;
    this.toggleMenu.emit(this.isFold);
  }

  private checkAccess(): void {
    if (!this.userService.isLoggedIn()) {
      this.isManager = false;
      this.isAdmin = false;
      return;
    }
  
    this.isManager = this.userService.hasRole('manager');
    this.isAdmin = this.userService.hasRole('admin');
  }
  
}
