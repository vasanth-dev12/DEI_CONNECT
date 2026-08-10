import { Component, EventEmitter, Output, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RoleLabelPipe } from '../../pipes/role-label.pipe';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RoleLabelPipe],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  readonly auth = inject(AuthService);
  readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  @Output() toggleSidebar = new EventEmitter<void>();

  logout(): void {
    this.auth.logout(false);
    this.router.navigate(['/login']);
  }
}
