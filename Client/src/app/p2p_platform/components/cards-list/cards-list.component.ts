import { Component, OnInit } from '@angular/core';
import { Card } from '../../models/card';
import { CardService } from '../../services/card.service';
import { CommonModule } from '@angular/common';
import { AddCardComponent } from '../add-card/add-card.component';

@Component({
  selector: 'app-cards-list',
  standalone: true,
  imports: [CommonModule, AddCardComponent],
  templateUrl: './cards-list.component.html',
  styleUrls: ['./cards-list.component.css']
})
export class CardsListComponent implements OnInit {
  cards: Card[] = [];
  isLoading = true;
  isListVisible: boolean = true;
  isAddCardModalVisible = false;

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

  showAddCardModal(): void {
    this.isAddCardModalVisible = true;
  }

  closeAddCardModal(): void {
    this.isAddCardModalVisible = false;
  }

  onCardAdded(newCard: Card): void {
    this.cards.push(newCard)
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