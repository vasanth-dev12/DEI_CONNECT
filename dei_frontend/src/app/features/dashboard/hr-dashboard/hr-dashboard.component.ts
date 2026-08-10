import { Component, OnInit, inject, signal } from '@angular/core';

import { UserService } from '../../../core/services/user.service';
import { PayEquityService } from '../../../core/services/pay-equity.service';
import { ReportService } from '../../../core/services/report.service';
import { PayEquityAnalysisResponse } from '../../../core/models/pay-equity.model';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-hr-dashboard',
  standalone: true,
  imports: [StatCardComponent, EmptyStateComponent, StatusBadgeComponent, EnumLabelPipe],
  templateUrl: './hr-dashboard.component.html',
})

export class HrDashboardComponent implements OnInit {
  private readonly users = inject(UserService);
  private readonly payEquity = inject(PayEquityService);
  private readonly reports = inject(ReportService);

  readonly assignedEmployees = signal(0);
  readonly totalAnalyses = signal(0);
  readonly totalReports = signal(0);
  readonly recentAnalyses = signal<PayEquityAnalysisResponse[]>([]);

  ngOnInit(): void {
    this.users.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.assignedEmployees.set(pagedResult.totalElements));
    this.payEquity.listAnalyses({ page: 0, size: 1 }).subscribe((pagedResult) => this.totalAnalyses.set(pagedResult.totalElements));
    this.payEquity.listAnalyses({ page: 0, size: 5 }).subscribe((pagedResult) => this.recentAnalyses.set(pagedResult.content));
    this.reports.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.totalReports.set(pagedResult.totalElements));
  }
}
