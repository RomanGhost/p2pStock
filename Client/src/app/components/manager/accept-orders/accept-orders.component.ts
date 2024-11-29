import { Component, NgZone, OnInit } from '@angular/core';
import { OrderInfo } from '../../../models/order';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../../services/order.service';
import { PaginationResponse } from '../../../models/pagination';
import { WebSocketService } from '../../../socket-services/web-socket.service';

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


  constructor(
    private orderService: OrderService,
    private webSocketServiceOrder: WebSocketService,
    private zone: NgZone
  ) {}
  
  ngOnInit(): void {
    this.loadOrders();
    this.connectSocket();
  }

  connectSocket():void{
    if (!this.webSocketServiceOrder.socket || this.webSocketServiceOrder.socket.readyState === WebSocket.CLOSED) {
      this.webSocketServiceOrder.connect('order');
    }

    this.webSocketServiceOrder.subscribeToMessages((updatedOrder: OrderInfo) => {
      this.zone.run(() => {
        this.handleOrderUpdate(updatedOrder);
      });
    });
  }

  ngOnDestroy(): void{
    this.webSocketServiceOrder.disconnect();
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

  private handleOrderUpdate(updatedOrder: OrderInfo): void {
    const orderIndex = this.orders.findIndex((order) => order.id === updatedOrder.id);
    
     if (this.isOrderMatchingFilters(updatedOrder)) {
      if (orderIndex !== -1) {
        // Если заказ существует, обновляем его
        this.orders[orderIndex] = updatedOrder;
      } else {
        this.orders.push(updatedOrder);
      }
    } else {
      // Если заказ не проходит по фильтрам, удаляем его
      this.orders.splice(orderIndex, 1);
    }
  
    console.log('Обновление заказа через WebSocket:', updatedOrder);
  }
  

  private isOrderMatchingFilters(order:OrderInfo): boolean{
    return (order.statusName === this.orderStatus);
  }
}
