import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { loginGuard } from './core/guards/login.guard';
import { AuthLayoutComponent } from './shared/layout/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './shared/layout/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      {
        path: 'login',
        canActivate: [loginGuard],
        loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
      },
      {
        path: 'forgot-password',
        loadComponent: () =>
          import('./features/auth/forgot-password/forgot-password.component').then((m) => m.ForgotPasswordComponent),
      },
      {
        path: 'access-denied',
        loadComponent: () =>
          import('./features/errors/access-denied/access-denied.component').then((m) => m.AccessDeniedComponent),
      },
    ],
  },

  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard-host/dashboard-host.component').then((m) => m.DashboardHostComponent),
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile.component').then((m) => m.ProfileComponent),
      },
      {
        path: 'users',
        canActivate: [roleGuard],
        data: { roles: ['DEI_MANAGER', 'HR_BIZ_PARTNER', 'ERG_LEAD', 'EXECUTIVE', 'ADMIN'] },
        loadChildren: () => import('./features/users/users.routes').then((m) => m.USERS_ROUTES),
      },
      {
        path: 'surveys',
        canActivate: [roleGuard],
        data: { roles: ['EMPLOYEE', 'DEI_MANAGER', 'EXECUTIVE', 'ADMIN'] },
        loadChildren: () => import('./features/surveys/surveys.routes').then((m) => m.SURVEYS_ROUTES),
      },
      {
        path: 'diversity',
        loadChildren: () => import('./features/diversity/diversity.routes').then((m) => m.DIVERSITY_ROUTES),
      },
      {
        path: 'goals',
        canActivate: [roleGuard],
        data: { roles: ['DEI_MANAGER', 'EXECUTIVE', 'ADMIN'] },
        loadChildren: () => import('./features/goals/goals.routes').then((m) => m.GOALS_ROUTES),
      },
      {
        path: 'pay-equity',
        loadChildren: () => import('./features/pay-equity/pay-equity.routes').then((m) => m.PAYEQUITY_ROUTES),
      },
      {
        path: 'ergs',
        canActivate: [roleGuard],
        data: { roles: ['EMPLOYEE', 'DEI_MANAGER', 'HR_BIZ_PARTNER', 'ERG_LEAD', 'EXECUTIVE', 'ADMIN'] },
        loadChildren: () => import('./features/ergs/ergs.routes').then((m) => m.ERGS_ROUTES),
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: { roles: ['DEI_MANAGER', 'HR_BIZ_PARTNER', 'EXECUTIVE', 'ADMIN'] },
        loadChildren: () => import('./features/reports/reports.routes').then((m) => m.REPORTS_ROUTES),
      },
      {
        path: 'notifications',
        loadChildren: () =>
          import('./features/notifications/notifications.routes').then((m) => m.NOTIFICATIONS_ROUTES),
      },
      {
        path: 'audit-logs',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('./features/audit/audit-log-list/audit-log-list.component').then((m) => m.AuditLogListComponent),
      },
    ],
  },

  {
    path: '**',
    loadComponent: () => import('./features/errors/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
];
