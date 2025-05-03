import { Component, OnInit } from '@angular/core';
import { Wallet } from '../../models/wallet';
import { WalletService } from '../../services/wallet.service';
import { CommonModule } from '@angular/common';
import { AddWalletComponent } from '../add-wallet/add-wallet.component';

@Component({
  selector: 'app-wallets-list',
  standalone: true,
  imports: [CommonModule, AddWalletComponent],
  templateUrl: './wallets-list.component.html',
  styleUrls: ['./wallets-list.component.css']
})
export class WalletsListComponent implements OnInit {
  wallets: Wallet[] = [];
  isLoading = true;
  isListVisible: boolean = true;
  isAddWalletModalVisible = false;

  constructor(private walletService: WalletService) {}

  ngOnInit(): void {
    this.loadWallets();
  }

  toggleListVisibility(): void {
    this.isListVisible = !this.isListVisible; // Переключаем состояние видимости
  }

  loadWallets(): void {
    this.isLoading = true;
    this.walletService.getUserWallets().subscribe({
      next: (wallets) => {
        this.wallets = wallets;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка при загрузке кошельков:', error);
        this.isLoading = false;
      }
    });
  }

  showAddWalletModal(): void {
    this.isAddWalletModalVisible = true;
  }

  closeAddWalletModal(): void {
    this.isAddWalletModalVisible = false;
  }

  onWalletAdded(newWallet: Wallet): void {
    this.wallets.push(newWallet);
  }

  deleteWallet(wallet: Wallet): void {
    if (confirm(`Вы уверены, что хотите удалить кошелек "${wallet.walletName}"?`)) {
      this.walletService.deleteWallet(wallet.id).subscribe({
        next: () => {
          this.wallets = this.wallets.filter(w => w.id !== wallet.id);
        },
        error: (error) => {
          console.error('Ошибка при удалении кошелька:', error);
        }
      });
    }
  }
}
