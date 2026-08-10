import { Component, Input } from '@angular/core';
import { humanize, statusColor } from '../../../core/constants/labels';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  templateUrl: './status-badge.component.html',
})
export class StatusBadgeComponent {
  @Input() set status(value: string | null | undefined) {
    this.text = humanize(value) || '—';
    this.color = statusColor(value);
  }
  text = '—';
  color = 'secondary';
}
