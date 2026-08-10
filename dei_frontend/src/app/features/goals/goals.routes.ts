import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const GOALS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./goal-list/goal-list.component').then((m) => m.GoalListComponent),
  },
  {
    path: 'new',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER'] },
    loadComponent: () => import('./goal-form/goal-form.component').then((m) => m.GoalFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./goal-detail/goal-detail.component').then((m) => m.GoalDetailComponent),
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER'] },
    loadComponent: () => import('./goal-form/goal-form.component').then((m) => m.GoalFormComponent),
  },
];
