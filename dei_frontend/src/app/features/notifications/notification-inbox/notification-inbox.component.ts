import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { NotificationService } from '../../../core/services/notification.service';
import { ToastService } from '../../../core/services/toast.service';
import { NotificationResponse } from '../../../core/models/notification.model';
import { ALL_NOTIFICATION_STATUSES, NotificationStatus } from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-notification-inbox',
  standalone: true,
  imports: [
    DatePipe, FormsModule, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './notification-inbox.component.html',
})
export class NotificationInboxComponent implements OnInit {
  private readonly notifications = inject(NotificationService);
  private readonly toast = inject(ToastService);

  readonly allStatuses = ALL_NOTIFICATION_STATUSES;
  statusFilter: NotificationStatus | null = null;

  readonly page = signal<Page<NotificationResponse> | null>(null);
  private currentPage = 0;

  ngOnInit(): void {
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.notifications
      .list({ page: pageIndex, size: 10, sort: 'createdDate,desc' }, this.statusFilter)
      .subscribe((pagedResult) => this.page.set(pagedResult));
  }

  markRead(notification: NotificationResponse): void {
    this.notifications.markRead(notification.notificationId).subscribe(() => this.reload(this.currentPage));
  }

  dismiss(notification: NotificationResponse): void {
    this.notifications.dismiss(notification.notificationId).subscribe(() => this.reload(this.currentPage));
  }

  markAllRead(): void {
    this.notifications.markAllRead().subscribe(() => {
      this.toast.success('All notifications marked as read.');
      this.reload(this.currentPage);
    });
  }
}
