import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { PayEquityService } from '../../../core/services/pay-equity.service';
import { PublishedPayEquityAnalysisResponse } from '../../../core/models/pay-equity.model';
import { Page } from '../../../core/models/common.model';
import { humanize } from '../../../core/constants/labels';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-published-list',
  standalone: true,
  imports: [
    DecimalPipe, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, EnumLabelPipe,
  ],
  templateUrl: './published-list.component.html',
})
export class PublishedListComponent implements OnInit {
  private readonly payEquity = inject(PayEquityService);

  readonly page = signal<Page<PublishedPayEquityAnalysisResponse> | null>(null);

  ngOnInit(): void {
    this.reload(0);
  }

  controlVariablesLabel(analysis: PublishedPayEquityAnalysisResponse): string {
    return analysis.controlVariables.map((variable) => humanize(variable)).join(', ') || '—';
  }

  reload(pageIndex: number): void {
    this.payEquity.listPublished({ page: pageIndex, size: 10, sort: 'createdDate,desc' }).subscribe((pagedResult) => this.page.set(pagedResult));
  }
}
