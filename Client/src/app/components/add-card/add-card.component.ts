// src/app/components/add-card/add-card.component.ts
import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { map, Observable } from 'rxjs';
import { DataService } from '../../services/data.service';
import { Data } from '../../models/data';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-card',
  templateUrl: './add-card.component.html',
  styleUrls: ['./add-card.component.css', '../../../assets/styles/modal-style.css'],
  standalone: true, 
  imports: [CommonModule]
})
export class AddCardComponent implements OnInit {
  banks$: Observable<Data[]> | undefined; // Поток банков
  @Output() closeModal = new EventEmitter<void>(); // Эмиттер для закрытия модального окна

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    this.banks$ = this.dataService.getBanks().pipe(
      map((banks: Data[]) => banks.sort((a, b) => a.name.localeCompare(b.name)))
    );

  }

  onSubmit(): void {
    // Логика отправки формы
    console.log('Форма отправлена');
    this.closeModal.emit(); // Закрыть модальное окно после отправки
  }

  onClose(): void {
    this.closeModal.emit(); // Закрыть модальное окно при отмене
  }
}
