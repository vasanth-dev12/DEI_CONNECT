import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ErgService } from '../../../core/services/erg.service';
import { ToastService } from '../../../core/services/toast.service';
import { MembershipResponse, UpdateMembershipRequest } from '../../../core/models/erg.model';
import { ALL_MEMBERSHIP_ROLES, ALL_MEMBERSHIP_STATUSES } from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-erg-members',
  standalone: true,
  imports: [
    DatePipe, ReactiveFormsModule, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './erg-members.component.html',
})
export class ErgMembersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly ergs = inject(ErgService);
  private readonly toast = inject(ToastService);

  readonly id = input.required<string>();

  readonly membershipRoles = ALL_MEMBERSHIP_ROLES;
  readonly membershipStatuses = ALL_MEMBERSHIP_STATUSES;

  readonly page = signal<Page<MembershipResponse> | null>(null);
  readonly editingId = signal<number | null>(null);
  readonly saving = signal(false);
  private currentPage = 0;

  readonly editForm = this.fb.group({
    role: ['MEMBER', [Validators.required]],
    status: ['ACTIVE', [Validators.required]],
  });

  ngOnInit(): void {
    this.reload(0);
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.ergs.listMembers(Number(this.id()), { page: pageIndex, size: 10, sort: 'joinDate,desc' }).subscribe((pagedResult) => this.page.set(pagedResult));
  }

  startEdit(membership: MembershipResponse): void {
    this.editingId.set(membership.membershipId);
    this.editForm.reset({ role: membership.role, status: membership.status });
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  saveEdit(membership: MembershipResponse): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      this.toast.error('Please correct the highlighted fields.');
      return;
    }
    this.saving.set(true);
    const formValue = this.editForm.getRawValue();
    const body: UpdateMembershipRequest = {
      role: formValue.role as UpdateMembershipRequest['role'],
      status: formValue.status as UpdateMembershipRequest['status'],
    };
    this.ergs.updateMembership(Number(this.id()), membership.membershipId, body).subscribe({
      next: () => {
        this.toast.success('Membership updated.');
        this.saving.set(false);
        this.editingId.set(null);
        this.reload(this.currentPage);
      },
      error: () => this.saving.set(false),
    });
  }
}
