import { Component, OnInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DealService } from '../../services/deal.service';
import { DealInfo } from '../../models/deal';
import { PaginationResponse } from '../../models/pagination';
import { UserInfo } from '../../models/user';
import { UserService } from '../../services/user.service';
import { Router } from '@angular/router';
import { WebSocketService } from '../../socket-services/web-socket.service';

@Component({
  selector: 'app-deal-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './deal-requests.component.html',
  styleUrls: ['./deal-requests.component.css'],
})
export class DealRequestsComponent implements OnInit, OnDestroy {
  deals: DealInfo[] = [];
  currentPage = 0;
  totalPages = 0;
  pageSize = 10;
  isLoading = false;
  errorMessage: string | null = null;
  user: UserInfo | undefined;

  // Фильтры
  filters = {
    changedAfter: '',
    sortOrder: 'desc',
  };

  constructor(
    private dealService: DealService,
    private userService: UserService,
    private router: Router,
    private webSocketServiceDeal: WebSocketService,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.userService.getUserProfile().subscribe({
      next: (user: UserInfo) => {
        this.user = user;
      },
      error: () => {
        console.error('User not found');
        this.router.navigate(['/login']);
      },
    });

    this.loadDeals();
    this.connectSocket();
  }

  ngOnDestroy(): void {
    this.webSocketServiceDeal.disconnect();
  }

  connectSocket():void{
    if (!this.webSocketServiceDeal.socket || this.webSocketServiceDeal.socket.readyState === WebSocket.CLOSED) {
      this.webSocketServiceDeal.connect('deal');
    }

    this.webSocketServiceDeal.subscribeToMessages((updatedDeal: DealInfo) => {
      this.zone.run(() => {
        this.handleDealUpdate(updatedDeal);
      });
    });
  }

  /**
   * Загрузка сделок с учётом фильтров
   */
  loadDeals(page: number = this.currentPage): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.dealService
      .getFilteredDeals({
        changedAfter: this.filters.changedAfter,
        sortOrder: this.filters.sortOrder,
        page,
        size: this.pageSize,
      })
      .subscribe({
        next: (response: PaginationResponse<DealInfo>) => {
          this.deals = response.content;
          this.totalPages = response.page.totalPages;
          this.currentPage = response.page.number;
        },
        error: (error) => {
          console.error('Ошибка загрузки сделок:', error);
          this.errorMessage = 'Не удалось загрузить сделки. Попробуйте позже.';
        },
        complete: () => (this.isLoading = false),
      });
  }

  /**
   * Применение фильтров
   */
  applyFilters(): void {
    this.currentPage = 0; // Сбрасываем на первую страницу при изменении фильтров
    this.loadDeals();
  }

  /**
   * Переход на указанную страницу
   * @param page Номер страницы
   */
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.loadDeals(page);
    }
  }

  isAssociatedWithDeal(deal: DealInfo): boolean {
    return (
      deal.buyOrder.userLogin === this.user?.login || deal.sellOrder.userLogin === this.user?.login
    );
  }

  wasCreatedEarlier(deal: DealInfo): boolean {
    const buyOrderCreationDate = new Date(deal.buyOrder.createdAt).getTime();
    const sellOrderCreationDate = new Date(deal.sellOrder.createdAt).getTime();
    if (buyOrderCreationDate < sellOrderCreationDate) {
      return deal.buyOrder.userLogin === this.user?.login;
    } else {
      return deal.sellOrder.userLogin === this.user?.login;
    }
  }

  confirmDeal(dealId: number): void {
    this.dealService.nextConfirm(dealId).subscribe({
      next: (deal: DealInfo) => {
        console.log(`Подтверждена сделка: ${dealId}`);
      },
      error: (err) => {
        console.error(err);
      },
    });
  }

  cancelDeal(dealId: number): void {
    this.dealService.nextReject(dealId).subscribe({
      next: (deal: DealInfo) => {
        console.log(`Отклонена сделка: ${dealId}`);
      },
      error: (err) => {
        console.error(err);
      },
    });
  }

  nextPage(): void {
    this.goToPage(this.currentPage + 1);
  }

  prevPage(): void {
    this.goToPage(this.currentPage - 1);
  }

  /**
   * Обработка обновления сделки через WebSocket
   */
  private handleDealUpdate(updatedDeal: DealInfo): void {
    const dealIndex = this.deals.findIndex((deal) => deal.id === updatedDeal.id);

    if (dealIndex !== -1) {
      this.deals[dealIndex] = updatedDeal;
    } else if (this.isDealMatchingFilters(updatedDeal)) {
      this.deals.push(updatedDeal);
    }
  }

  /**
   * Проверяем, соответствует ли сделка текущим фильтрам
   */
  private isDealMatchingFilters(deal: DealInfo): boolean {
    return new Date(deal.lastStatusChange) >= new Date(this.filters.changedAfter);
  }
}
