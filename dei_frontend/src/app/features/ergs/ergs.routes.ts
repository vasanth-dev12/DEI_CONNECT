import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const ERGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./erg-list/erg-list.component').then((m) => m.ErgListComponent),
  },
  {
    path: 'new',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER'] },
    loadComponent: () => import('./erg-form/erg-form.component').then((m) => m.ErgFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./erg-detail/erg-detail.component').then((m) => m.ErgDetailComponent),
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER', 'ADMIN', 'ERG_LEAD'] },
    loadComponent: () => import('./erg-form/erg-form.component').then((m) => m.ErgFormComponent),
  },
  {
    path: ':id/members',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER', 'ERG_LEAD', 'EXECUTIVE', 'ADMIN'] },
    loadComponent: () => import('./erg-members/erg-members.component').then((m) => m.ErgMembersComponent),
  },
  {
    path: ':id/events/new',
    canActivate: [roleGuard],
    data: { roles: ['ERG_LEAD'] },
    loadComponent: () => import('./erg-event-form/erg-event-form.component').then((m) => m.ErgEventFormComponent),
  },
  {
    path: ':id/events/:eventId',
    loadComponent: () => import('./erg-event-detail/erg-event-detail.component').then((m) => m.ErgEventDetailComponent),
  },
  {
    path: ':id/events/:eventId/edit',
    canActivate: [roleGuard],
    data: { roles: ['ERG_LEAD'] },
    loadComponent: () => import('./erg-event-form/erg-event-form.component').then((m) => m.ErgEventFormComponent),
  },
];
