import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { UserInfo } from '../../models/user';
import { AddCardComponent } from '../add-card/add-card.component';
import { AddWalletComponent } from '../add-wallet/add-wallet.component';
import { Observable } from 'rxjs';
import { Card } from '../../models/card';
import { Wallet } from '../../models/wallet';
import { DealRequestsComponent } from '../deal-requests/deal-requests.component';
import { CardsListComponent } from "../cards-list/cards-list.component";
import { WalletsListComponent } from "../wallets-list/wallets-list.component";

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css'],
  standalone: true,
  imports: [CommonModule , DealRequestsComponent, CardsListComponent, WalletsListComponent]
})
export class AccountComponent implements OnInit {
  @ViewChild(WalletsListComponent) walletsListComponent!: WalletsListComponent;

  user$: Observable<UserInfo | undefined>;
  isSidebarOpen = false;
  selectedWallet: Wallet | null = null;
  selectedCard: Card | null = null;

  constructor(
    private userService: UserService,
  ) {
    this.user$ = this.userService.getUserProfile();
  }

  ngOnInit(): void {
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }


  clearSelection(): void {
    this.selectedWallet = null;
    this.selectedCard = null;
  }
}