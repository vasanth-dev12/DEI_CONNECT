import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './stat-card.component.html',
})
export class StatCardComponent {
  @Input() label = '';
  @Input() value: string | number = '';
  @Input() icon = 'bi-graph-up';
  @Input() color = 'primary';
  @Input() hint = '';
  @Input() link: string | null = null;
  @Input() queryParams: Record<string, string | number | boolean> | null = null;
}
