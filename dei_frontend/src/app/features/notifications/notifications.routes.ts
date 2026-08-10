import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const NOTIFICATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./notification-inbox/notification-inbox.component').then((m) => m.NotificationInboxComponent),
  },
  {
    path: 'emit',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./emit-notification/emit-notification.component').then((m) => m.EmitNotificationComponent),
  },
];
