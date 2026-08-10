import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'danger' | 'warning' | 'info';

export interface Toast {
  id: number;
  kind: ToastKind;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  success(message: string)
  {
     this.push('success', message);
  }
  error(message: string)
  {
    this.push('danger', message);
  }
  warning(message: string)
   {
     this.push('warning', message);
    }
  info(message: string)
  {
    this.push('info', message);
   }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((toast) => toast.id !== id));
  }

  private push(kind: ToastKind, message: string): void {
    const id = ++this.seq;
    this.toasts.update((list) => [...list, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), 5000);
  }
}
