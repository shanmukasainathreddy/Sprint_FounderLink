import { CommonModule, CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AppStore } from '../app.store';

@Component({
  selector: 'app-auth-page',
  imports: [CommonModule, FormsModule, CurrencyPipe],
  templateUrl: './auth-page.component.html',
  styleUrl: './auth-page.component.css',
})
export class AuthPageComponent {
  readonly store = inject(AppStore);
  private readonly router = inject(Router);

  async login(): Promise<void> {
    if (await this.store.login()) {
      this.router.navigate(['/dashboard']);
    }
  }

  async register(): Promise<void> {
    await this.store.register();
  }

  async verifyOtp(): Promise<void> {
    await this.store.verifyOtp();
  }

  async resendOtp(): Promise<void> {
    await this.store.resendOtp();
  }

  async requestPasswordResetOtp(): Promise<void> {
    await this.store.requestPasswordResetOtp();
  }

  async verifyPasswordResetOtp(): Promise<void> {
    await this.store.verifyPasswordResetOtp();
  }

  async resetPassword(): Promise<void> {
    await this.store.resetPassword();
  }
}
