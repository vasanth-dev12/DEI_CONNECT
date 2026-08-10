import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { SurveyService } from '../../../core/services/survey.service';
import { ErgService } from '../../../core/services/erg.service';
import { NotificationService } from '../../../core/services/notification.service';
import { SurveyResponse } from '../../../core/models/survey.model';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [RouterLink, StatCardComponent, EmptyStateComponent, EnumLabelPipe],
  templateUrl: './employee-dashboard.component.html',
})
export class EmployeeDashboardComponent implements OnInit {
  private readonly surveys = inject(SurveyService);
  private readonly ergs = inject(ErgService);
  private readonly notifications = inject(NotificationService);

  readonly activeSurveys = signal<SurveyResponse[]>([]);
  readonly totalErgs = signal(0);
  readonly unreadNotifications = signal(0);

  readonly awaitingResponse = computed(() => this.activeSurveys().filter((survey) => !survey.respondedByMe));
  readonly completedCount = computed(() => this.activeSurveys().filter((survey) => survey.respondedByMe).length);

  ngOnInit(): void {
    this.surveys.list({ page: 0, size: 50 }).subscribe((pagedResult) => {
      this.activeSurveys.set(pagedResult.content.filter((survey) => survey.status === 'ACTIVE'));
    });

    this.ergs.list({ page: 0, size: 50 }).subscribe((pagedResult) => this.totalErgs.set(pagedResult.totalElements));
    this.notifications.list({ page: 0, size: 1 }, 'UNREAD').subscribe((pagedResult) => this.unreadNotifications.set(pagedResult.totalElements));
  }
}
