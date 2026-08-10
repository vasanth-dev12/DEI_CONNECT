import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const SURVEYS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./survey-list/survey-list.component').then((m) => m.SurveyListComponent),
  },
  {
    path: 'new',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./survey-form/survey-form.component').then((m) => m.SurveyFormComponent),
  },
  {
    path: ':id',
    loadComponent: () => import('./survey-detail/survey-detail.component').then((m) => m.SurveyDetailComponent),
  },
  {
    path: ':id/edit',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./survey-form/survey-form.component').then((m) => m.SurveyFormComponent),
  },
  {
    path: ':id/questions',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER', 'ADMIN'] },
    loadComponent: () => import('./survey-questions/survey-questions.component').then((m) => m.SurveyQuestionsComponent),
  },
  {
    path: ':id/respond',
    canActivate: [roleGuard],
    data: { roles: ['EMPLOYEE'] },
    loadComponent: () => import('./survey-respond/survey-respond.component').then((m) => m.SurveyRespondComponent),
  },
  {
    path: ':id/summaries',
    canActivate: [roleGuard],
    data: { roles: ['DEI_MANAGER', 'EXECUTIVE', 'ADMIN'] },
    loadComponent: () => import('./survey-summaries/survey-summaries.component').then((m) => m.SurveySummariesComponent),
  },
];
