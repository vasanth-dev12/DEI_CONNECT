import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PayEquityService } from '../../../core/services/pay-equity.service';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../core/services/toast.service';
import { PayEquityAnalysisResponse } from '../../../core/models/pay-equity.model';
import { ALL_ANALYSIS_STATUSES, ALL_PAY_DIMENSIONS, AnalysisStatus, PayDimension } from '../../../core/models/enums';
import { UserResponse } from '../../../core/models/iam.model';
import { Page } from '../../../core/models/common.model';
import { humanize } from '../../../core/constants/labels';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-analysis-list',
  standalone: true,
  imports: [
    DecimalPipe, FormsModule, RouterLink, PageHeaderComponent, PaginatorComponent,
    EmptyStateComponent, StatusBadgeComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './analysis-list.component.html',
})
export class AnalysisListComponent implements OnInit {
  private readonly payEquity = inject(PayEquityService);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);

  readonly dimensions = ALL_PAY_DIMENSIONS;
  readonly statuses = ALL_ANALYSIS_STATUSES;

  dimensionFilter: PayDimension | null = null;
  statusFilter: AnalysisStatus | null = null;
  hrIdFilter: number | null = null;

  readonly page = signal<Page<PayEquityAnalysisResponse> | null>(null);
  readonly hrUsers = signal<UserResponse[]>([]);
  private currentPage = 0;

  ngOnInit(): void {
    this.users.listByRole('HR_BIZ_PARTNER', 200).subscribe((pagedResult) => this.hrUsers.set(pagedResult.content));
    this.reload(0);
  }

  controlVariablesLabel(analysis: PayEquityAnalysisResponse): string {
    return analysis.controlVariables.map((variable) => humanize(variable)).join(', ') || '—';
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.payEquity
      .listAnalyses(
        { page: pageIndex, size: 10, sort: 'createdDate,desc' },
        { dimension: this.dimensionFilter, status: this.statusFilter, hrId: this.hrIdFilter },
      )
      .subscribe((pagedResult) => this.page.set(pagedResult));
  }

  publish(analysis: PayEquityAnalysisResponse): void {
    this.payEquity.publishAnalysis(analysis.id).subscribe(() => {
      this.toast.success('Analysis published.');
      this.reload(this.currentPage);
    });
  }
}
