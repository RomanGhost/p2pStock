import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { CommonModule } from '@angular/common';
import { DataService } from '../../services/data.service';
import { Data } from '../../models/data';
import { map, Observable } from 'rxjs';

@Component({
  selector: 'app-add-order',
  templateUrl: './add-order.component.html',
  styleUrls: ['./add-order.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})

export class AddOrderComponent implements OnInit {
  orderForm: FormGroup;
  types$: Observable<Data[]> | undefined;; // Типы заказов
  wallets: any[] = []; // Кошельки
  cards: any[] = []; // Карты
  walletError: string | null = null;
  cardError: string | null = null;
  priceError: string | null = null;
  quantityError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private orderService: OrderService,  // Сервис для работы с заказами
    private router: Router,
    private dataService: DataService,
  ) {
    // Инициализация формы в конструкторе
    this.orderForm = this.fb.group({
      orderType: ['', Validators.required],
      wallet: ['', Validators.required],
      card: ['', Validators.required],
      pricePerUnit: ['', [Validators.required, Validators.min(0)]],
      quantity: ['', [Validators.required, Validators.min(0)]],
      description: ['']
    });
  }

  ngOnInit(): void {
    // Загрузка данных (например, типы заказов, кошельки, карты) с сервера
    this.loadData();
  }

  loadData(): void {
    // Пример загрузки данных
    // Здесь вы можете использовать ваш сервис для загрузки типов, кошельков и карт.
    this.types$ = this.dataService.getOrderTypes().pipe(
      map((getTypes: Data[]) => getTypes.sort((a, b) => a.name.localeCompare(b.name)))
    );
    
    this.wallets = [{id: 1, name: 'Кошелек 1'}, {id: 2, name: 'Кошелек 2'}];  // Заглушка
    this.cards = [{id: 1, cardName: 'Карта 1'}, {id: 2, cardName: 'Карта 2'}];  // Заглушка
  }

  onSubmit(): void {
    if (this.orderForm.invalid) {
      return;
    }

    const formData = this.orderForm.value;

    // Пример обращения к API для сохранения данных
    this.orderService.saveOrder(formData).subscribe(
      (response) => {
        this.router.navigate(['/orders']);  // Переход на страницу заказов
      },
      (error) => {
        // Обработка ошибок
        console.error(error);
      }
    );
  }
}