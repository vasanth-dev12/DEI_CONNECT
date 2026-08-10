import { Component, inject } from '@angular/core';

import { AuthService } from '../../../core/auth/auth.service';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

import { AdminDashboardComponent } from '../admin-dashboard/admin-dashboard.component';
import { HrDashboardComponent } from '../hr-dashboard/hr-dashboard.component';
import { ManagerDashboardComponent } from '../manager-dashboard/manager-dashboard.component';
import { ErgLeadDashboardComponent } from '../erg-lead-dashboard/erg-lead-dashboard.component';
import { EmployeeDashboardComponent } from '../employee-dashboard/employee-dashboard.component';
import { ExecutiveDashboardComponent } from '../executive-dashboard/executive-dashboard.component';

@Component({
  selector: 'app-dashboard-host',
  standalone: true,
  imports: [
    PageHeaderComponent,
    AdminDashboardComponent,
    HrDashboardComponent,
    ManagerDashboardComponent,
    ErgLeadDashboardComponent,
    EmployeeDashboardComponent,
    ExecutiveDashboardComponent,
  ],
  templateUrl: './dashboard-host.component.html',
})
export class DashboardHostComponent {
  readonly auth = inject(AuthService);

  greeting(): string {
    return `Welcome, ${this.auth.currentUser()?.name ?? ''}`;
  }
}
