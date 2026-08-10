import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ReportService } from '../../../core/services/report.service';
import { DEIReportDataResponse, DEIReportResponse } from '../../../core/models/reporting.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-report-data',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, RouterLink, PageHeaderComponent, StatusBadgeComponent,
    StatCardComponent, EmptyStateComponent, EnumLabelPipe,
  ],
  templateUrl: './report-data.component.html',
})
export class ReportDataComponent implements OnInit {
  private readonly reports = inject(ReportService);
  private readonly decimalPipe = new DecimalPipe('en-US');

  readonly id = input.required<string>();
  readonly report = signal<DEIReportResponse | null>(null);
  readonly data = signal<DEIReportDataResponse | null>(null);

  ngOnInit(): void {
    this.reports.getById(Number(this.id())).subscribe((response) => this.report.set(response));
    this.reports.getData(Number(this.id())).subscribe((dataResponse) => this.data.set(dataResponse));
  }

  formatNumber(value: number): string {
    return this.decimalPipe.transform(value, '1.0-2') ?? String(value);
  }

  formatPercent(value: number): string {
    return this.formatNumber(value) + '%';
  }
}
