import { Component, OnInit, inject, signal } from '@angular/core';

import { UserService } from '../../../core/services/user.service';
import { SurveyService } from '../../../core/services/survey.service';
import { ErgService } from '../../../core/services/erg.service';
import { ReportService } from '../../../core/services/report.service';
import { DiversityService } from '../../../core/services/diversity.service';
import { PayEquityService } from '../../../core/services/pay-equity.service';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [StatCardComponent],
  templateUrl: './admin-dashboard.component.html',
})
export class AdminDashboardComponent implements OnInit {
  private readonly users = inject(UserService);
  private readonly surveys = inject(SurveyService);
  private readonly ergs = inject(ErgService);
  private readonly reports = inject(ReportService);
  private readonly diversity = inject(DiversityService);
  private readonly payEquity = inject(PayEquityService);

  readonly totalUsers = signal(0);
  readonly employeeCount = signal(0);
  readonly managerCount = signal(0);
  readonly hrCount = signal(0);
  readonly ergLeadCount = signal(0);
  readonly executiveCount = signal(0);
  readonly totalSurveys = signal(0);
  readonly totalErgs = signal(0);
  readonly totalSnapshots = signal(0);
  readonly totalAnalyses = signal(0);
  readonly totalReports = signal(0);

  ngOnInit(): void {
    const firstRow = { page: 0, size: 1 };
    this.users.list(firstRow).subscribe((r) => this.totalUsers.set(r.totalElements));
    this.users.list(firstRow, 'EMPLOYEE').subscribe((r) => this.employeeCount.set(r.totalElements));
    this.users.list(firstRow, 'DEI_MANAGER').subscribe((r) => this.managerCount.set(r.totalElements));
    this.users.list(firstRow, 'HR_BIZ_PARTNER').subscribe((r) => this.hrCount.set(r.totalElements));
    this.users.list(firstRow, 'ERG_LEAD').subscribe((r) => this.ergLeadCount.set(r.totalElements));
    this.users.list(firstRow, 'EXECUTIVE').subscribe((r) => this.executiveCount.set(r.totalElements));
    this.surveys.list(firstRow).subscribe((r) => this.totalSurveys.set(r.totalElements));
    this.ergs.list(firstRow).subscribe((r) => this.totalErgs.set(r.totalElements));
    this.diversity.listSnapshotRuns(firstRow).subscribe((r) => this.totalSnapshots.set(r.totalElements));
    this.payEquity.listAnalyses(firstRow).subscribe((r) => this.totalAnalyses.set(r.totalElements));
    this.reports.list(firstRow).subscribe((r) => this.totalReports.set(r.totalElements));
  }
}
