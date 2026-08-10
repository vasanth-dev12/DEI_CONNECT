import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { GoalService } from '../../../core/services/goal.service';
import { GoalResponse } from '../../../core/models/goal.model';
import { ALL_GOAL_DIMENSIONS, ALL_GOAL_STATUSES, GoalDimension, GoalStatus } from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-goal-list',
  standalone: true,
  imports: [
    DecimalPipe, FormsModule, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './goal-list.component.html',
})
export class GoalListComponent implements OnInit {
  private readonly goals = inject(GoalService);

  readonly allDimensions = ALL_GOAL_DIMENSIONS;
  readonly allStatuses = ALL_GOAL_STATUSES;
  dimensionFilter: GoalDimension | null = null;
  statusFilter: GoalStatus | null = null;

  readonly page = signal<Page<GoalResponse> | null>(null);
  private currentPage = 0;

  ngOnInit(): void {
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.goals
      .list(
        { page: pageIndex, size: 10, sort: 'createdDate,desc' },
        { dimension: this.dimensionFilter, status: this.statusFilter },
      )
      .subscribe((pagedResult) => this.page.set(pagedResult));
  }
}
