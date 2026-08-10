import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserResponse } from '../../../core/models/iam.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { RoleLabelPipe } from '../../../shared/pipes/role-label.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink, PageHeaderComponent, StatusBadgeComponent, HasRoleDirective, RoleLabelPipe, EnumLabelPipe],
  templateUrl: './user-detail.component.html',
})
export class UserDetailComponent implements OnInit {
  private readonly users = inject(UserService);
  readonly auth = inject(AuthService);

  readonly id = input.required<string>();
  readonly user = signal<UserResponse | null>(null);

  ngOnInit(): void {
    this.users.getById(Number(this.id())).subscribe((user) => this.user.set(user));
  }
}
