import { Component, OnInit, inject, signal } from '@angular/core';

import { UserService } from '../../../core/services/user.service';
import { SurveyService } from '../../../core/services/survey.service';
import { ErgService } from '../../../core/services/erg.service';
import { GoalService } from '../../../core/services/goal.service';
import { ReportService } from '../../../core/services/report.service';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-executive-dashboard',
  standalone: true,
  imports: [StatCardComponent],
  templateUrl: './executive-dashboard.component.html',
})
export class ExecutiveDashboardComponent implements OnInit {
  private readonly users = inject(UserService);
  private readonly surveys = inject(SurveyService);
  private readonly ergs = inject(ErgService);
  private readonly goals = inject(GoalService);
  private readonly reports = inject(ReportService);

  readonly totalEmployees = signal(0);
  readonly totalSurveys = signal(0);
  readonly totalErgs = signal(0);
  readonly totalGoals = signal(0);
  readonly publishedReports = signal(0);

  ngOnInit(): void {
    this.users.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.totalEmployees.set(pagedResult.totalElements));
    this.surveys.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.totalSurveys.set(pagedResult.totalElements));
    this.ergs.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.totalErgs.set(pagedResult.totalElements));
    this.goals.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.totalGoals.set(pagedResult.totalElements));
    this.reports.list({ page: 0, size: 1 }, 'PUBLISHED').subscribe((pagedResult) => this.publishedReports.set(pagedResult.totalElements));
  }
}
