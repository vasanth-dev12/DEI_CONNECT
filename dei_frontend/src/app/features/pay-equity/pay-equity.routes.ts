import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const PAYEQUITY_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'published' },
  {
    path: 'analyses',
    canActivate: [roleGuard],
    data: { roles: ['HR_BIZ_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./analysis-list/analysis-list.component').then((m) => m.AnalysisListComponent),
  },
  {
    path: 'analyses/new',
    canActivate: [roleGuard],
    data: { roles: ['HR_BIZ_PARTNER'] },
    loadComponent: () => import('./analysis-form/analysis-form.component').then((m) => m.AnalysisFormComponent),
  },
  {
    path: 'analyses/:id',
    canActivate: [roleGuard],
    data: { roles: ['HR_BIZ_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./analysis-detail/analysis-detail.component').then((m) => m.AnalysisDetailComponent),
  },
  {
    path: 'analyses/:id/edit',
    canActivate: [roleGuard],
    data: { roles: ['HR_BIZ_PARTNER'] },
    loadComponent: () => import('./analysis-form/analysis-form.component').then((m) => m.AnalysisFormComponent),
  },
  {
    path: 'published',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER', 'EXECUTIVE', 'HR_BIZ_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./published-list/published-list.component').then((m) => m.PublishedListComponent),
  },
  {
    path: 'published/:id',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER', 'EXECUTIVE', 'HR_BIZ_PARTNER', 'ADMIN'] },
    loadComponent: () => import('./published-detail/published-detail.component').then((m) => m.PublishedDetailComponent),
  },
];
