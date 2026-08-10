import { Component, OnInit, inject, signal } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { ErgService } from '../../../core/services/erg.service';
import { ToastService } from '../../../core/services/toast.service';
import { ErgResponse } from '../../../core/models/erg.model';
import { ALL_ERG_FOCUSES, ALL_ERG_STATUSES, ErgFocus, ErgStatus } from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-erg-list',
  standalone: true,
  imports: [
    SlicePipe, FormsModule, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, ConfirmModalComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './erg-list.component.html',
})
export class ErgListComponent implements OnInit {
  private readonly ergs = inject(ErgService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly allFocuses = ALL_ERG_FOCUSES;
  readonly allStatuses = ALL_ERG_STATUSES;
  focusFilter: ErgFocus | null = null;
  statusFilter: ErgStatus | null = null;

  readonly page = signal<Page<ErgResponse> | null>(null);
  readonly toDelete = signal<ErgResponse | null>(null);
  private currentPage = 0;

  ngOnInit(): void {
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.ergs
      .list({ page: pageIndex, size: 9, sort: 'ergName,asc' }, { focus: this.focusFilter, status: this.statusFilter })
      .subscribe((pagedResult) => this.page.set(pagedResult));
  }

  canManage(erg: ErgResponse): boolean {
    const userId = this.auth.currentUser()?.userId;
    switch (this.auth.role()) {
      case 'ADMIN':
        return true;
      case 'DEI_MANAGER':
        return erg.creatorManagerId === userId;
      case 'ERG_LEAD':
        return erg.ergLeadId === userId;
      default:
        return false;
    }
  }

  canViewMembers(erg: ErgResponse): boolean {
    return this.canManage(erg) || this.auth.hasAnyRole(['EXECUTIVE']);
  }

  askDelete(erg: ErgResponse): void {
    this.toDelete.set(erg);
  }

  confirmDelete(): void {
    const erg = this.toDelete();
    if (!erg) return;
    this.ergs.delete(erg.ergId).subscribe(() => {
      this.toast.success(`${erg.ergName} deleted.`);
      this.toDelete.set(null);
      this.reload(this.currentPage);
    });
  }
}
