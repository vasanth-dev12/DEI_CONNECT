import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { SurveyService } from '../../../core/services/survey.service';
import { ToastService } from '../../../core/services/toast.service';
import { SurveyResponse } from '../../../core/models/survey.model';
import { ALL_SURVEY_STATUSES, SurveyStatus } from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-survey-list',
  standalone: true,
  imports: [
    DatePipe, FormsModule, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, ConfirmModalComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './survey-list.component.html',
})
export class SurveyListComponent implements OnInit {
  private readonly surveys = inject(SurveyService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);

  readonly allStatuses = ALL_SURVEY_STATUSES;
  statusFilter: SurveyStatus | null = null;

  readonly page = signal<Page<SurveyResponse> | null>(null);
  readonly toDelete = signal<SurveyResponse | null>(null);
  private currentPage = 0;

  readonly isEmployee = this.auth.role() === 'EMPLOYEE';

  ngOnInit(): void {
    this.reload(0);
  }

  canManage(): boolean {
    return this.auth.role() === 'ADMIN';
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.surveys.list({ page: pageIndex, size: 10 }, this.statusFilter).subscribe((pagedResult) => this.page.set(pagedResult));
  }

  launch(survey: SurveyResponse): void {
    this.surveys.launch(survey.surveyId).subscribe(() => {
      this.toast.success(`${survey.surveyName} launched.`);
      this.reload(this.currentPage);
    });
  }

  close(survey: SurveyResponse): void {
    this.surveys.close(survey.surveyId).subscribe(() => {
      this.toast.success(`${survey.surveyName} closed.`);
      this.reload(this.currentPage);
    });
  }

  publish(survey: SurveyResponse): void {
    this.surveys.publish(survey.surveyId).subscribe(() => {
      this.toast.success(`${survey.surveyName} published.`);
      this.reload(this.currentPage);
    });
  }

  askDelete(survey: SurveyResponse): void {
    this.toDelete.set(survey);
  }

  confirmDelete(): void {
    const survey = this.toDelete();
    if (!survey) return;
    this.surveys.delete(survey.surveyId).subscribe(() => {
      this.toast.success(`${survey.surveyName} deleted.`);
      this.toDelete.set(null);
      this.reload(this.currentPage);
    });
  }
}
