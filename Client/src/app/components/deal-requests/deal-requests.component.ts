import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DealService } from '../../services/deal.service';
import { DealInfo } from '../../models/deal';
import { PaginationResponse } from '../../models/pagination';

@Component({
  selector: 'app-deal-requests',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './deal-requests.component.html',
  styleUrls: ['./deal-requests.component.css'],
})
export class DealRequestsComponent implements OnInit {
  deals: DealInfo[] = [];
  currentPage = 0;
  totalPages = 0;
  pageSize = 10;

  isLoading = false;

  constructor(private dealService: DealService) {}

  ngOnInit(): void {
    this.loadDeals();
  }

  /**
   * Загрузка сделок с сервера
   */
  loadDeals(page: number = this.currentPage): void {
    this.isLoading = true;
    this.dealService.getFilteredDeals(undefined, undefined, 'desc', page, this.pageSize).subscribe({
      next: (response: PaginationResponse<DealInfo>) => {
        this.deals = response.content;
        this.totalPages = response.page.totalPages;
        this.currentPage = response.page.number;
      },
      error: (error) => console.error('Ошибка загрузки сделок:', error),
      complete: () => (this.isLoading = false),
    });
  }

  /**
   * Обработчик перехода на следующую страницу
   */
  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.loadDeals(this.currentPage + 1);
    }
  }

  /**
   * Обработчик возврата на предыдущую страницу
   */
  prevPage(): void {
    if (this.currentPage > 0) {
      this.loadDeals(this.currentPage - 1);
    }
  }
}
