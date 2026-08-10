import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-paginator',
  standalone: true,
  templateUrl: './paginator.component.html',
})
export class PaginatorComponent {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Output() pageChange = new EventEmitter<number>();

  go(p: number): void {
    if (p < 0 || p > this.totalPages - 1 || p === this.page) return;
    this.pageChange.emit(p);
  }

  pages(): number[] {
    const start = Math.max(0, Math.min(this.page - 2, this.totalPages - 5));
    const end = Math.min(this.totalPages, start + 5);
    const arr: number[] = [];
    for (let i = start; i < end; i++) arr.push(i);
    return arr;
  }
}
