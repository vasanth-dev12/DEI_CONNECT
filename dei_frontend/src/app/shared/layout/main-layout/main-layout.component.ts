import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent],
  templateUrl: './main-layout.component.html',
})
export class MainLayoutComponent implements OnInit {
  private readonly notifications = inject(NotificationService);
  readonly mobileOpen = signal(false);

  ngOnInit(): void {
    this.notifications.refreshUnreadCount().subscribe({ error: () => {} });
  }
}
