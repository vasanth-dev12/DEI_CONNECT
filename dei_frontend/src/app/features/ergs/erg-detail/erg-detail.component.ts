import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { ErgService } from '../../../core/services/erg.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ErgResponse, EventResponse, MembershipResponse } from '../../../core/models/erg.model';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-erg-detail',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, ConfirmModalComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './erg-detail.component.html',
})
export class ErgDetailComponent implements OnInit {
  private readonly ergs = inject(ErgService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input.required<string>();
  readonly erg = signal<ErgResponse | null>(null);
  readonly notFound = signal(false);

  readonly membershipLoaded = signal(false);
  readonly isActiveMember = signal(false);
  readonly membershipBusy = signal(false);

  readonly eventsPage = signal<Page<EventResponse> | null>(null);
  private currentEventsPage = 0;

  readonly confirmingDelete = signal(false);

  readonly isEmployee = this.auth.role() === 'EMPLOYEE';

  canManage(): boolean {
    const erg = this.erg();
    if (!erg) return false;
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

  canViewMembers(): boolean {
    return this.canManage() || this.auth.hasAnyRole(['EXECUTIVE']);
  }

  ngOnInit(): void {
    this.ergs.getById(Number(this.id())).subscribe({
      next: (ergResponse) => this.erg.set(ergResponse),
      error: () => this.notFound.set(true),
    });

    if (this.isEmployee) {
      this.loadMembership();
    } else {
      this.reloadEvents(0);
    }
  }

  canViewEvents(): boolean {
    return !this.isEmployee || this.isActiveMember();
  }

  private loadMembership(): void {
    this.ergs.myMembership(Number(this.id())).subscribe({
      next: (membership: MembershipResponse) => {
        this.isActiveMember.set(membership.status === 'ACTIVE');
        this.membershipLoaded.set(true);
        if (membership.status === 'ACTIVE') this.reloadEvents(0);
      },
      error: () => {
        this.isActiveMember.set(false);
        this.membershipLoaded.set(true);
      },
    });
  }

  join(): void {
    this.membershipBusy.set(true);
    this.ergs.join(Number(this.id())).subscribe({
      next: () => {
        this.toast.success('You joined the group.');
        this.membershipBusy.set(false);
        this.loadMembership();
        this.ergs.getById(Number(this.id())).subscribe((ergResponse) => this.erg.set(ergResponse));
      },
      error: () => this.membershipBusy.set(false),
    });
  }

  leave(): void {
    this.membershipBusy.set(true);
    this.ergs.leave(Number(this.id())).subscribe({
      next: () => {
        this.toast.success('You left the group.');
        this.membershipBusy.set(false);
        this.loadMembership();
        this.ergs.getById(Number(this.id())).subscribe((ergResponse) => this.erg.set(ergResponse));
      },
      error: () => this.membershipBusy.set(false),
    });
  }

  reloadEvents(pageIndex: number): void {
    this.currentEventsPage = pageIndex;
    this.ergs.listEvents(Number(this.id()), { page: pageIndex, size: 10, sort: 'date,desc' }).subscribe((pagedEvents) => this.eventsPage.set(pagedEvents));
  }

  askDelete(): void {
    this.confirmingDelete.set(true);
  }

  confirmDelete(): void {
    this.ergs.delete(Number(this.id())).subscribe(() => {
      this.toast.success('ERG deleted.');
      this.confirmingDelete.set(false);
      this.router.navigate(['/ergs']);
    });
  }
}
