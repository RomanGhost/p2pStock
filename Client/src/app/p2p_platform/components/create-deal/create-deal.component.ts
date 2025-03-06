import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { OrderInfo } from '../../models/order';
import { CardService } from '../../services/card.service';
import { WalletService } from '../../services/wallet.service';
import { Wallet } from '../../models/wallet';
import { Card } from '../../models/card';
import { DealService } from '../../services/deal.service';
import { CreateDealInfo, DealInfo } from '../../models/deal';

type NewType = Wallet;

@Component({
  selector: 'app-create-deal',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './create-deal.component.html',
  styleUrl: './create-deal.component.css'
})
export class CreateDealComponent implements OnInit {
//Модальное окно
dealForm!: FormGroup;
@Input() selectedOrder: OrderInfo | null = null;
wallets: NewType[] = [];
cards: Card[] = [];

// @Output() dealCreated = new EventEmitter<void>(); // Событие создания сделки
@Output() closeModalEvent = new EventEmitter<void>(); // Событие закрытия модального окна

  constructor(
    private fb: FormBuilder, 
    private dealService: DealService,
    private cardService: CardService,
    private walletService: WalletService,
  ) {
  }

  ngOnInit(){
    // console. log(`Order: ${this.selectedOrder?.cryptocurrencyCode}`)
    this.dealForm = this.fb.group({
      wallet: [-1, Validators.required], 
      card: [-1, Validators.required]
    });

    this.loadUserWallets();
    this.loadUserCards();
  }

  closeModal() {
    this.closeModalEvent.emit(); // Уведомить родительский компонент о закрытии
  }

  createDeal() {
    if (this.dealForm.valid) {
      const valueDealForm = this.dealForm.value;

      const walletId = Number(valueDealForm.wallet);
      const cardId = Number(valueDealForm.card);

      var newDeal: CreateDealInfo = {
        walletId: walletId,
        cardId: cardId,
        counterpartyOrderId: this.selectedOrder!!.id
      };

      this.dealService.addDeal(newDeal).subscribe({
        next: (response: DealInfo)=>{
          console.log(`CreateDealComponent: Создана новая сделка: ${response.id}`);
        },
        error: (err) => {
          console.log(`CreateDealComponent: ${err}`);
        }
      });

      this.closeModal(); // Закрыть модальное окно
    }
  }


  // Загружаем кошельки и карты (заглушка для примера)
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
    this.walletService.getUserWallets(this.selectedOrder?.cryptocurrencyCode).subscribe({
      next: (wallets) => {
        this.wallets = wallets;
      },
      error: (error) => {
        console.error('Ошибка при загрузке кошельков:', error);
      }
    });
  }
}
