import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { SurveyService } from '../../../core/services/survey.service';
import { ToastService } from '../../../core/services/toast.service';
import { SummaryResponse } from '../../../core/models/survey.model';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-survey-summaries',
  standalone: true,
  imports: [
    DecimalPipe, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './survey-summaries.component.html',
})
export class SurveySummariesComponent implements OnInit {
  private readonly surveys = inject(SurveyService);
  private readonly toast = inject(ToastService);

  readonly id = input.required<string>();
  readonly page = signal<Page<SummaryResponse> | null>(null);
  private currentPage = 0;

  ngOnInit(): void {
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.surveys.listSummaries(Number(this.id()), { page: pageIndex, size: 10 }).subscribe((pagedResult) => this.page.set(pagedResult));
  }

  publish(summary: SummaryResponse): void {
    this.surveys.publishSummary(Number(this.id()), summary.summaryId).subscribe(() => {
      this.toast.success('Summary published.');
      this.reload(this.currentPage);
    });
  }
}
