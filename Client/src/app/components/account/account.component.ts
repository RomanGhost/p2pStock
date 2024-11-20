import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { UserInfo } from '../../models/user';
import { AddCardComponent } from '../add-card/add-card.component';
import { AddWalletComponent } from '../add-wallet/add-wallet.component';
import { Observable } from 'rxjs';
import { CardService } from '../../services/card.service';
import { WalletService } from '../../services/wallet.service';
import { Card } from '../../models/card';
import { Wallet } from '../../models/wallet';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css'],
  standalone: true,
  imports: [CommonModule, AddCardComponent, AddWalletComponent]
})
export class AccountComponent implements OnInit {
  user$: Observable<UserInfo | undefined>;
  cards: Card[] = [];
  wallets: Wallet[] = [];
  isAddCardModalVisible = false;
  isAddWalletModalVisible = false;
  isSidebarOpen = false;
  selectedWallet: Wallet | null = null;
  selectedCard: Card | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private cardService: CardService,
    private walletService: WalletService,
    private router: Router
  ) {
    this.user$ = this.userService.getUserProfile();
  }

  ngOnInit(): void {
    this.loadUserCards();
    this.loadUserWallets();
  }

  loadUserCards(): void {
    this.cardService.getUserCards().subscribe({
      next: (cards) => {
        this.cards = cards;
      },
      error: (error) => {
        console.error('Ошибка при загрузке карт:', error);
      }
    });
  }

  loadUserWallets(): void {
    this.walletService.getUserWallets().subscribe({
      next: (wallets) => {
        this.wallets = wallets;
      },
      error: (error) => {
        console.error('Ошибка при загрузке кошельков:', error);
      }
    });
  }

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
    this.loadUserCards(); // Обновляем карты после закрытия модального окна
  }

  showAddWalletModal(): void {
    this.isAddWalletModalVisible = true;
  }

  closeAddWalletModal(): void {
    this.isAddWalletModalVisible = false;
    this.loadUserWallets(); // Обновляем кошельки после закрытия модального окна
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  selectWallet(wallet: Wallet): void {
    this.selectedWallet = wallet;
  }

  selectCard(card: Card): void {
    this.selectedCard = card;
  }

  clearSelection(): void {
    this.selectedWallet = null;
    this.selectedCard = null;
  }

  deleteCard(card: Card): void {
    if (confirm(`Вы уверены, что хотите удалить карту "${card.cardName}"?`)) {
      this.cardService.deleteCard(card.id).subscribe(() => {
        this.cards = this.cards.filter(c => c.id !== card.id);
      });
    }
  }
  
  deleteWallet(wallet: Wallet): void {
    if (confirm(`Вы уверены, что хотите удалить кошелек "${wallet.walletName}"?`)) {
      this.walletService.deleteWallet(wallet.id).subscribe(() => {
        this.wallets = this.wallets.filter(w => w.id !== wallet.id);
      });
    }
  }
  

  
}
