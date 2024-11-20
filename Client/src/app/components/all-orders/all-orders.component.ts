import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { OrderInfo, OrderListResponse } from '../../models/order';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { DataService } from '../../services/data.service';
import { Data } from '../../models/data';
import { CreateDealComponent } from '../create-deal/create-deal.component';

@Component({
  selector: 'app-all-orders',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, CreateDealComponent],
  templateUrl: './all-orders.component.html',
  styleUrls: ['./all-orders.component.css']
})
export class AllOrdersComponent implements OnInit {
  orders: OrderInfo[] = [];
  filteredOrders: OrderInfo[] = [];
  uniqueCryptocurrencies: Data[] = [];
  uniqueOrderTypes: Data[] = [];
  availablePageSizes: number[] = [10, 20, 50];
  filterForm!: FormGroup;
  isCreateDealModalOpen = false;

  selectedStatus: string = 'Модерация';//'Доступна на платформе';

  loading = true;
  errorMessage: string | null = null;
  page: number = 0;
  size: number = 10;  // Размер страницы по умолчанию
  totalPages: number = 0;
  totalElements: number = 0;

  selectedOrder: any = null;

  constructor(
    private orderService: OrderService,
    private dataService:DataService,
    private fb: FormBuilder
  ) {
    this.size = this.availablePageSizes[0];
  }

  ngOnInit(): void {
    this.initializeForm();
    this.loadOrders();
    this.loadOrderStatuses();
    this.loadCryptocurrancy();
  }

  // Инициализация формы
  initializeForm(): void {
    this.filterForm = this.fb.group({
      type: [''],
      cryptoCode: [''],
      sortOrder: ['desc']  // По умолчанию сортировка по возрастанию
    });
  }

  // Загружаем заказы с фильтрами
  loadOrders(): void {
    this.loading = true;
    const { type, cryptoCode, sortOrder } = this.filterForm.value;
    this.orderService.getFilteredOrders(this.selectedStatus, type, cryptoCode, this.page, this.size, sortOrder)
      .subscribe({
        next: (response: OrderListResponse) => {
          this.orders = response.content;
          this.filteredOrders = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = 'Ошибка загрузки заказов.';
          this.loading = false;
        }
      });
  }

  loadOrderStatuses(): void {
    this.dataService.getOrderTypes().subscribe({
      next: (orderTypes) => {
        this.uniqueOrderTypes = orderTypes;
      },
      error: () => {
        console.log("Error load types");
      }
    });
    
  }

  loadCryptocurrancy(): void {
    this.dataService.getCryptocurrencies().subscribe({
      next: (cryptos) => {
        this.uniqueCryptocurrencies = cryptos;
      },
      error: () => {
        console.log("Error load cryptocurrencies");
      }
    });
  }

  // Применяем фильтры
  filterOrders(): void {
    this.page = 0; // Сбрасываем на первую страницу при изменении фильтров
    this.loadOrders();
  }

  // Изменяем страницу
  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.page = page;
      this.loadOrders();
    }
  }

  // Изменяем размер страницы
  onSizeChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const size = Number(target.value);
    this.size = size;
    this.page = 0;
    this.loadOrders();
  }

  // Проверка, нужно ли показывать кнопку "Назад"
  showBackButton(): boolean {
    return this.page > 0;
  }

  // Проверка, нужно ли показывать кнопку "Вперед"
  showForwardButton(): boolean {
    return this.page < this.totalPages - 1;
  }

  openCreateDealModal(order: OrderInfo): void {
    this.selectedOrder = order;
    this.isCreateDealModalOpen = true;
  }

  closeCreateDealModal(): void {
    this.isCreateDealModalOpen = false;
    this.selectedOrder = null;
  }


}
