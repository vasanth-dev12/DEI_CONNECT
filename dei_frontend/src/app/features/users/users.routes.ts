import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const USERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./user-list/user-list.component').then((m) => m.UserListComponent),
  },
  {
    path: 'new',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./user-form/user-form.component').then((m) => m.UserFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./user-detail/user-detail.component').then((m) => m.UserDetailComponent),
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./user-form/user-form.component').then((m) => m.UserFormComponent),
  },
];
