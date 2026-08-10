import { Component, EventEmitter, Output, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { NAV_ITEMS } from '../../../core/constants/nav-config';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private readonly auth = inject(AuthService);
  @Output() navigate = new EventEmitter<void>();

  readonly visibleItems = computed(() => {
    const role = this.auth.role();
    if (!role) return [];
    return NAV_ITEMS.filter((item) => item.roles.length === 0 || item.roles.includes(role));
  });
}
