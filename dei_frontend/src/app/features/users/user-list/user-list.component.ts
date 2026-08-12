import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { UserResponse } from '../../../core/models/iam.model';
import { ALL_ROLES, Role } from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { RoleLabelPipe } from '../../../shared/pipes/role-label.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { roleLabel } from '../../../core/constants/labels';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    FormsModule, RouterLink, PageHeaderComponent, PaginatorComponent, EmptyStateComponent,
    StatusBadgeComponent, ConfirmModalComponent, HasRoleDirective, RoleLabelPipe, EnumLabelPipe,
  ],
  templateUrl: './user-list.component.html',
})
export class UserListComponent implements OnInit {
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  readonly auth = inject(AuthService);

  readonly allRoles = ALL_ROLES;
  roleFilter: Role | null = null;

  readonly page = signal<Page<UserResponse> | null>(null);
  readonly toDeactivate = signal<UserResponse | null>(null);
  private currentPage = 0;

  ngOnInit(): void {
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const role = params.get('role') as Role | null;
      this.roleFilter = role && ALL_ROLES.includes(role) ? role : null;
      this.reload(0);
    });
  }

  onRoleFilterChange(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { role: this.roleFilter ?? null },
      queryParamsHandling: 'merge',
    });
  }

  label(role: Role): string { 
    return roleLabel(role); 
  }

  reload(pageIndex: number): void {
    this.currentPage = pageIndex;
    this.users.list({ page: pageIndex, size: 10, sort: 'name,asc' }, this.roleFilter)
    .subscribe((pagedResult) => this.page.set(pagedResult));
  }

  askDeactivate(user: UserResponse): void {
    this.toDeactivate.set(user);
  }

  confirmDeactivate(): void {
    const user = this.toDeactivate();
    if (!user) return;
    this.users.deactivate(user.userId).subscribe(() => {
      this.toast.success(`${user.name} deactivated.`);
      this.toDeactivate.set(null);
      this.reload(this.currentPage);
    });
  }
}
