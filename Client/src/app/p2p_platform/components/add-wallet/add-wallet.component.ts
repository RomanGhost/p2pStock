import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { map, Observable } from 'rxjs';
import { DataService } from '../../services/data.service';
import { Data } from '../../models/data';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { WalletService } from '../../services/wallet.service';
import { Wallet } from '../../models/wallet';

@Component({
  selector: 'app-add-wallet',
  templateUrl: './add-wallet.component.html',
  styleUrls: ['./add-wallet.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class AddWalletComponent implements OnInit {
  crypto$: Observable<Data[]> | undefined; 
  @Output() closeModal = new EventEmitter<void>(); 
  @Output() walletAdded = new EventEmitter<Wallet>();
  walletForm: FormGroup;
  errorMessage: string | null = null;

  constructor(
    private dataService: DataService,
    private fb: FormBuilder, 
    private walletService: WalletService
  ) {
    this.walletForm = this.fb.group({
      walletName: ['', Validators.required],
      crypto: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.crypto$ = this.dataService.getCryptocurrencies().pipe(
      map((crypto: Data[]) => crypto.sort((a, b) => a.name.localeCompare(b.name)))
    );

  }

  onSubmit(): void {
    if (this.walletForm.valid) {
      const formData = this.walletForm.value;
      const addNewWallet: Wallet = {
        id: 0,
        walletName: formData.walletName,
        balance: 0.0,
        cryptocurrencyCode: formData.crypto,
      };
      console.log(formData);

      this.walletService.addNewWallet(addNewWallet).subscribe({
        next: (response) => {
          console.log('Кошелек успешно добавлен:', response);
          this.walletAdded.emit(response);
          this.closeModal.emit();
        },
        error: (error) => {
          // Сохраняем сообщение об ошибке, чтобы показать пользователю
          this.errorMessage = error.message;
          console.error('Ошибка при добавлении кошелька:', error);
        }
      });
    } else {
      this.errorMessage = 'Форма содержит ошибки';
    }
  }

  onClose(): void {
    this.closeModal.emit(); // Закрыть модальное окно при отмене
  }

}
