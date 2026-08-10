import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ReportService } from '../../../core/services/report.service';
import { ToastService } from '../../../core/services/toast.service';
import { DEIReportResponse } from '../../../core/models/reporting.model';
import { ALL_REPORT_STATUSES, ReportStatus } from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-report-list',
  standalone: true,
  imports: [
    DatePipe, FormsModule, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, ConfirmModalComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './report-list.component.html',
})
export class ReportListComponent implements OnInit {
  private readonly reports = inject(ReportService);
  private readonly toast = inject(ToastService);

  readonly allStatuses = ALL_REPORT_STATUSES;
  statusFilter: ReportStatus | null = null;

  readonly page = signal<Page<DEIReportResponse> | null>(null);
  readonly toDelete = signal<DEIReportResponse | null>(null);
  private currentPage = 0;

  ngOnInit(): void {
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.reports.list({ page: pageIndex, size: 10 }, this.statusFilter).subscribe((pagedResult) => this.page.set(pagedResult));
  }

  publish(report: DEIReportResponse): void {
    this.reports.publish(report.id).subscribe(() => {
      this.toast.success('Report published.');
      this.reload(this.currentPage);
    });
  }

  askDelete(report: DEIReportResponse): void {
    this.toDelete.set(report);
  }

  confirmDelete(): void {
    const report = this.toDelete();
    if (!report) return;
    this.reports.delete(report.id).subscribe(() => {
      this.toast.success('Report deleted.');
      this.toDelete.set(null);
      this.reload(this.currentPage);
    });
  }
}
