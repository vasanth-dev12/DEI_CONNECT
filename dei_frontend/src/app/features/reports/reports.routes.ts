import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const REPORTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./report-list/report-list.component').then((m) => m.ReportListComponent),
  },
  {
    path: 'new',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./report-form/report-form.component').then((m) => m.ReportFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./report-data/report-data.component').then((m) => m.ReportDataComponent),
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./report-form/report-form.component').then((m) => m.ReportFormComponent),
  },
];
