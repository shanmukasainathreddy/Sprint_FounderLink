import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { AppStore } from './app.store';
import { AuthPageComponent } from './pages/auth-page.component';
import { DashboardPageComponent } from './pages/dashboard-page.component';
import { ServicePageComponent } from './pages/service-page.component';

const authGuard: CanActivateFn = () => {
  const store = inject(AppStore);
  const router = inject(Router);
  return store.isLoggedIn() ? true : router.createUrlTree(['/auth']);
};

const guestGuard: CanActivateFn = () => {
  const store = inject(AppStore);
  const router = inject(Router);
  return store.isLoggedIn() ? router.createUrlTree(['/dashboard']) : true;
};

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'auth' },
  { path: 'auth', component: AuthPageComponent, canActivate: [guestGuard] },
  { path: 'dashboard', component: DashboardPageComponent, canActivate: [authGuard] },
  { path: 'services/:service', component: ServicePageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'auth' },
];
