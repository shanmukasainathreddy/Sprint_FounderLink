import { CommonModule, CurrencyPipe, PercentPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AppStore, ServiceKey } from '../app.store';

@Component({
  selector: 'app-dashboard-page',
  imports: [CommonModule, CurrencyPipe, PercentPipe],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css',
})
export class DashboardPageComponent {
  readonly store = inject(AppStore);
  private readonly router = inject(Router);

  readonly featuredServices = computed(() =>
    this.store.accessibleServices().map((key) => ({
      key,
      ...this.store.serviceMeta[key],
    })),
  );

  openService(service: ServiceKey): void {
    this.router.navigate(['/services', service]);
  }

  logout(): void {
    this.store.logout();
    this.router.navigate(['/auth']);
  }
}
