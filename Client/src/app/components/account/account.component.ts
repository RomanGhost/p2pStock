// src/app/components/account/account.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { UserInfo } from '../../models/user';
import { AddCardComponent } from '../add-card/add-card.component';
import { AddWalletComponent } from '../add-wallet/add-wallet.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css'],
  standalone: true,
  imports: [CommonModule, AddCardComponent, AddWalletComponent]
})
export class AccountComponent implements OnInit {
  user$: Observable<UserInfo | undefined>;
  isAddCardModalVisible = false;
  isAddWalletModalVisible = false;
  isSidebarOpen = false;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {
    this.user$ = this.userService.getUserProfile();
  }

  ngOnInit() {}

  logout(): void {
    this.userService.clearCache();
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  showAddCardModal(): void {
    this.isAddCardModalVisible = true;
  }

  closeAddCardModal(): void {
    this.isAddCardModalVisible = false;
  }

  showAddWalletModal(): void {
    this.isAddWalletModalVisible = true;
  }

  closeAddWalletModal(): void {
    this.isAddWalletModalVisible = false;
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }
}
