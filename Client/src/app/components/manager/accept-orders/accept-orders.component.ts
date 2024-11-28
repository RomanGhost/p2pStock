import { Component, OnInit } from '@angular/core';
import { OrderInfo } from '../../../models/order';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../../services/order.service';
import { PaginationResponse } from '../../../models/pagination';

@Component({
  selector: 'app-accept-orders',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './accept-orders.component.html',
  styleUrl: './accept-orders.component.css'
})
export class AcceptOrdersComponent implements OnInit{
  orders: OrderInfo[] = [];
  isLoading = false;
  orderStatus: string = 'Модерация';

  //Пагинация
  totalPages = 0;
  totalElements = 0;
  page = 0;
  size = 10;
  sortOrder = 'asc';


  constructor(private orderService: OrderService) {}
  
  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.orderService.getFilteredOrders(this.orderStatus, "", "", this.page, this.size, this.sortOrder)
      .subscribe({
        next: (response: PaginationResponse<OrderInfo>) => {
          this.orders = response.content;
          this.totalPages = response.page.totalPages;
          this.totalElements = response.page.totalElements;
        },
        error: (err) => {
          // this.errorMessage = 'Ошибка загрузки заказов.';
        }
      });
      this.isLoading = false;
  }

  confirmOrder(order: OrderInfo): void {
    if (confirm(`Вы уверены, что хотите подтвердить сделку #${order.id}?`)) {
      this.orderService.acceptModeration(order.id).subscribe({
        next: () => console.log("Сделка успешно подтверждена"),
        error: () => console.log("Подтверждение сделки с ошибкой"),
      }
      );
    }
  }

  rejectOrder(order: OrderInfo): void {
    if (confirm(`Вы уверены, что хотите отказать в сделке #${order.id}?`)) {
      this.orderService.rejectModeration(order.id).subscribe();
      console.log("Сделка успешно отказана");
    }
  }

  applyFilters(): void {
    this.page = 0; // Сбросить пагинацию при изменении фильтров
    this.loadOrders();
  }

  nextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadOrders();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadOrders();
    }
  }
}
