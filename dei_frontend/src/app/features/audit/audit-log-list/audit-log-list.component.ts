import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AuditService } from '../../../core/services/audit.service';
import { AuditLogResponse } from '../../../core/models/iam.model';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-audit-log-list',
  standalone: true,
  imports: [DatePipe, FormsModule, PageHeaderComponent, PaginatorComponent, EmptyStateComponent],
  templateUrl: './audit-log-list.component.html',
})
export class AuditLogListComponent implements OnInit {
  private readonly audit = inject(AuditService);

  userIdFilter: number | null = null;
  entityTypeFilter = '';

  readonly page = signal<Page<AuditLogResponse> | null>(null);
  private currentPage = 0;

  ngOnInit(): void {
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.audit
      .list(
        { page: pageIndex, size: 10, sort: 'timestamp,desc' },
        { userId: this.userIdFilter, entityType: this.entityTypeFilter || null },
      )
      .subscribe((pagedResult) => this.page.set(pagedResult));
  }
}
