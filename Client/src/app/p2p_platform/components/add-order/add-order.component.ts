import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { CommonModule } from '@angular/common';
import { DataService } from '../../services/data.service';
import { Data } from '../../models/data';
import { map, Observable } from 'rxjs';
import { CardService } from '../../services/card.service';
import { WalletService } from '../../services/wallet.service';
import { CreateOrderInfo, OrderInfo } from '../../models/order';

@Component({
  selector: 'app-add-order',
  templateUrl: './add-order.component.html',
  styleUrls: ['./add-order.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})

export class AddOrderComponent implements OnInit {
  orderForm: FormGroup;
  types$: Observable<Data[]> | undefined;
  wallets: any[] = [];
  cards: any[] = [];
  walletError: string | null = null;
  cardError: string | null = null;
  priceError: string | null = null;
  quantityError: string | null = null;
  serverError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private orderService: OrderService,
    private router: Router,
    private dataService: DataService,
    private cardService: CardService,
    private walletService: WalletService
  ) {
    this.orderForm = this.fb.group({
      orderType: ['', Validators.required],
      wallet: ['', Validators.required],
      card: ['', Validators.required],
      pricePerUnit: ['', [Validators.required, Validators.min(0.1)]],
      quantity: ['', [Validators.required, Validators.min(1)]],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.types$ = this.dataService.getOrderTypes();
    this.loadUserWallets();
    this.loadUserCards();
  }

  loadUserWallets(): void {
    this.walletService.getUserWallets().subscribe({
      next: (wallets) => {
        this.wallets = wallets;
        if (wallets.length > 0) {
          this.orderForm.patchValue({ wallet: wallets[0].id });
        }
      },
      error: () => {
        this.walletError = 'Ошибка загрузки кошельков';
      }
    });
  }

  loadUserCards(): void {
    this.cardService.getUserCards().subscribe({
      next: (cards) => {
        this.cards = cards;
        if (cards.length > 0) {
          this.orderForm.patchValue({ card: cards[0].id });
        }
      },
      error: () => {
        this.cardError = 'Ошибка загрузки карт';
      }
    });
  }

  onSubmit(): void {
    // console.log(this.orderForm.value);
    // console.log(orderForm);
    if (this.orderForm.invalid) {
      return;
    }
    const valueOrderForm = this.orderForm.value;
    const orderInfo:CreateOrderInfo = {
      walletId:valueOrderForm.wallet,
      cardId:valueOrderForm.card,
      typeName:valueOrderForm.orderType,
      statusName:"",
      unitPrice:valueOrderForm.pricePerUnit,
      quantity:valueOrderForm.quantity,
      description:valueOrderForm.description,
    };

    console.log(orderInfo);

    this.orderService.addOrder(orderInfo).subscribe({
      next: () => {
        this.router.navigate(['/orders']); // Переход на страницу заказов
      },
      error: (error) => {
        this.serverError = `Ошибка создания заказа: ${error.message}`;
      }
    });
  }
}