import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { UserService } from '../../../core/services/user.service';
import { SurveyService } from '../../../core/services/survey.service';
import { ErgService } from '../../../core/services/erg.service';
import { GoalService } from '../../../core/services/goal.service';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [RouterLink, StatCardComponent],
  templateUrl: './manager-dashboard.component.html',
})

export class ManagerDashboardComponent implements OnInit {
  private readonly users = inject(UserService);
  private readonly surveys = inject(SurveyService);
  private readonly ergs = inject(ErgService);
  private readonly goals = inject(GoalService);

  readonly assignedEmployees = signal(0);
  readonly mySurveys = signal(0);
  readonly myErgs = signal(0);
  readonly myGoals = signal(0);

  ngOnInit(): void {
    this.users.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.assignedEmployees.set(pagedResult.totalElements));
    this.surveys.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.mySurveys.set(pagedResult.totalElements));
    this.ergs.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.myErgs.set(pagedResult.totalElements));
    this.goals.list({ page: 0, size: 1 }).subscribe((pagedResult) => this.myGoals.set(pagedResult.totalElements));
  }
}
