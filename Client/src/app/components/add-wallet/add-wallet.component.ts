import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { map, Observable } from 'rxjs';
import { DataService } from '../../services/data.service';
import { Data } from '../../models/data';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-wallet',
  templateUrl: './add-wallet.component.html',
  styleUrls: ['./add-wallet.component.css', '../../../assets/styles/modal-style.css'],
  standalone: true,
  imports: [CommonModule]
})
export class AddWalletComponent implements OnInit {
  crypto$: Observable<Data[]> | undefined; 
  @Output() closeModal = new EventEmitter<void>(); 

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    this.crypto$ = this.dataService.getCryptocurrencies().pipe(
      map((crypto: Data[]) => crypto.sort((a, b) => a.name.localeCompare(b.name)))
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
