import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const DIVERSITY_ROUTES: Routes = [
  {
    path: 'my-profile',
    canActivate: [roleGuard],
    data: { roles: ['EMPLOYEE'] },
    loadComponent: () => import('./my-demographic-profile/my-demographic-profile.component').then((m) => m.MyDemographicProfileComponent),
  },
  {
    path: 'snapshots',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER', 'EXECUTIVE', 'ADMIN'] },
    loadComponent: () => import('./representation-snapshots/representation-snapshots.component').then((m) => m.RepresentationSnapshotsComponent),
  },
];
