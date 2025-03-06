import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { map, Observable } from 'rxjs';
import { DataService } from '../../services/data.service';
import { Data } from '../../models/data';
import { Card } from '../../models/card';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CardService } from '../../services/card.service';

@Component({
  selector: 'app-add-card',
  templateUrl: './add-card.component.html',
  styleUrls: ['./add-card.component.css', '../../../assets/styles/modal-style.css'],
  standalone: true, 
  imports: [CommonModule, ReactiveFormsModule]
})
export class AddCardComponent implements OnInit {
  banks$: Observable<Data[]> | undefined;
  @Output() closeModal = new EventEmitter<void>();
  @Output() cardAdded = new EventEmitter<Card>();
  cardForm: FormGroup;
  errorMessage: string | null = null;

  constructor(
    private dataService: DataService, 
    private fb: FormBuilder, 
    private cardService: CardService
  ) {
    this.cardForm = this.fb.group({
      cardName: ['', Validators.required],
      cardNumber: ['', [Validators.required, Validators.pattern(/^\d{12,16}$/)]],
      bank: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.banks$ = this.dataService.getBanks().pipe(
      map((banks: Data[]) => banks
        .sort((a, b) => a.name.localeCompare(b.name))
        .map((bank, index) => ({ ...bank, code: (index + 1).toString() }))
      )
    );
  }

  onSubmit(): void {
    if (this.cardForm.valid) {
      const formData = this.cardForm.value;
      const newCard: Card = {
        id: 0,
        cardName: formData.cardName,
        cardNumber: formData.cardNumber,
        bankName: formData.bank
      };
      this.cardService.addNewCard(newCard).subscribe({
        next: (response) => {
          console.log('Карта успешно добавлена:', response);
          this.cardAdded.emit(response); // Передаем добавленную карту
          this.closeModal.emit();
        },
        error: (error) => {
          this.errorMessage = error.message;
          console.error('Ошибка при добавлении карты:', error);
        }
      });
    } else {
      this.errorMessage = 'Форма содержит ошибки';
    }
  }

  onClose(): void {
    this.closeModal.emit();
  }
}