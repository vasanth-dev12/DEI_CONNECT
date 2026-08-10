import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { PayEquityService } from '../../../core/services/pay-equity.service';
import { PublishedPayEquityAnalysisResponse, PublishedPayGapFlagResponse } from '../../../core/models/pay-equity.model';
import { humanize } from '../../../core/constants/labels';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-published-detail',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink, PageHeaderComponent, EmptyStateComponent, StatusBadgeComponent, EnumLabelPipe],
  templateUrl: './published-detail.component.html',
})
export class PublishedDetailComponent implements OnInit {
  private readonly payEquity = inject(PayEquityService);

  readonly id = input.required<string>();

  readonly analysis = signal<PublishedPayEquityAnalysisResponse | null>(null);
  readonly flags = signal<PublishedPayGapFlagResponse[] | null>(null);

  ngOnInit(): void {
    this.payEquity.getPublished(Number(this.id())).subscribe((analysis) => this.analysis.set(analysis));
    this.payEquity.listPublishedFlags(Number(this.id())).subscribe((flags) => this.flags.set(flags));
  }

  controlVariablesLabel(analysis: PublishedPayEquityAnalysisResponse): string {
    return analysis.controlVariables.map((variable) => humanize(variable)).join(', ') || '—';
  }
}
