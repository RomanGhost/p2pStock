import { Component, OnInit } from '@angular/core';
import { Card } from '../../models/card';
import { CardService } from '../../services/card.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cards-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cards-list.component.html',
  styleUrls: ['./cards-list.component.css', '../../../assets/styles/list-styles.css']
})
export class CardsListComponent implements OnInit {
  cards: Card[] = [];
  isLoading = true;
  isListVisible: boolean = true;

  constructor(private cardService: CardService) {}

  ngOnInit(): void {
    this.loadCards();
  }

  toggleListVisibility(): void {
    this.isListVisible = !this.isListVisible;
  }

  public loadCards(): void {
    this.isLoading = true;
    this.cardService.getUserCards().subscribe({
      next: (cards) => {
        this.cards = cards;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка при загрузке карт:', error);
        this.isLoading = false;
      }
    });
  }

  deleteCard(card: Card): void {
    if (confirm(`Вы уверены, что хотите удалить карту "${card.cardName}"?`)) {
      this.cardService.deleteCard(card.id).subscribe({
        next: () => {
          this.cards = this.cards.filter(c => c.id !== card.id);
        },
        error: (error) => {
          console.error('Ошибка при удалении карты:', error);
        }
      });
    }
  }
}