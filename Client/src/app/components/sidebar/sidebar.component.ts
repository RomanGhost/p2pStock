import { Component, OnInit } from '@angular/core';
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
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
})
export class SidebarComponent implements OnInit {
  isLoggedIn = false;
  isManager = false;
  isFold=false;

  constructor(private userService: UserService, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.isManager = this.userService.hasRole('manager');
    this.authService.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status; 
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  switchView(){
    this.isFold = !this.isFold;
  }
}
