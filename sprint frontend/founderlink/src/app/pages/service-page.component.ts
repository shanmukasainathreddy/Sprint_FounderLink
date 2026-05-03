import { CommonModule, CurrencyPipe, DatePipe, PercentPipe, TitleCasePipe } from '@angular/common';
import { Component, computed, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { map } from 'rxjs';
import { AppStore, InvestmentStatus, ListingStatus, ServiceKey } from '../app.store';

@Component({
  selector: 'app-service-page',
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    RouterLinkActive,
    CurrencyPipe,
    DatePipe,
    PercentPipe,
    TitleCasePipe,
  ],
  templateUrl: './service-page.component.html',
  styleUrl: './service-page.component.css',
})
export class ServicePageComponent {
  readonly store = inject(AppStore);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private lastService: ServiceKey | null = null;

  private readonly serviceParam = toSignal(
    this.route.paramMap.pipe(map((params) => params.get('service') as ServiceKey | null)),
    { initialValue: this.route.snapshot.paramMap.get('service') as ServiceKey | null },
  );

  readonly service = computed(() => {
    const service = this.serviceParam();
    return service && this.store.serviceMeta[service] ? service : 'profiles';
  });
  readonly meta = computed(() => this.store.serviceMeta[this.service()]);

  constructor() {
    effect(() => {
      const accessible = this.store.accessibleServices();
      const service = this.service();
      if (this.lastService && this.lastService !== service) {
        this.store.clearBackendMessage();
      }
      this.lastService = service;
      if (accessible.length && !accessible.includes(service)) {
        this.router.navigate(['/services', accessible[0]], { replaceUrl: true });
      }
    });
  }

  logout(): void {
    this.store.logout();
    this.router.navigate(['/auth']);
  }

  openMessages(partnerId: string): void {
    this.store.openConversation(partnerId);
    this.router.navigate(['/services/messages']);
  }

  openInvestments(startupId: string): void {
    this.store.useSelectedStartupForInvestment(startupId);
    this.router.navigate(['/services/investments']);
  }

  editStartup(startupId: string): void {
    this.store.editStartup(startupId);
    this.router.navigate(['/services/startups']);
  }

  reviewStartup(startupId: string, decision: ListingStatus): void {
    this.store.reviewStartup(startupId, decision);
  }

  canSetInvestmentStatus(currentStatus: InvestmentStatus, nextStatus: InvestmentStatus): boolean {
    if (currentStatus === 'PENDING') {
      return nextStatus === 'APPROVED' || nextStatus === 'REJECTED';
    }
    if (currentStatus === 'APPROVED') {
      return nextStatus === 'COMPLETED';
    }
    return false;
  }

  investmentStatusHint(status: InvestmentStatus): string {
    if (status === 'PENDING') {
      return 'Waiting for founder review. Approve or reject this request.';
    }
    if (status === 'APPROVED') {
      return 'Approved. Approval is locked; complete it after the funds are received.';
    }
    if (status === 'COMPLETED') {
      return 'Completed. This investment is finalized.';
    }
    return 'Rejected. This investment request is closed.';
  }

  updateInvestmentStatus(investmentId: string, status: InvestmentStatus, currentStatus?: InvestmentStatus): void {
    if (currentStatus && !this.canSetInvestmentStatus(currentStatus, status)) {
      return;
    }
    this.store.updateInvestmentStatus(investmentId, status);
  }
}
