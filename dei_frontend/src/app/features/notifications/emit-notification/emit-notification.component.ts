import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../core/services/toast.service';
import { EmitNotificationRequest, NotificationResponse } from '../../../core/models/notification.model';
import { UserResponse } from '../../../core/models/iam.model';
import { ALL_NOTIFICATION_CATEGORIES } from '../../../core/models/enums';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-emit-notification',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './emit-notification.component.html',
})
export class EmitNotificationComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly notifications = inject(NotificationService);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly categories = ALL_NOTIFICATION_CATEGORIES;
  readonly saving = signal(false);

  readonly recipients = signal<UserResponse[]>([]);

  readonly lastDelivered = signal<NotificationResponse | null>(null);

  readonly form = this.fb.group({
    employeeId: ['', [Validators.required]],
    category: ['SURVEY', [Validators.required]],
    message: ['', [Validators.required, Validators.maxLength(500)]],
  });

  ngOnInit(): void {
    this.users.list({ page: 0, size: 500, sort: 'name,asc' }).subscribe((pagedResult) => {
      this.recipients.set(pagedResult.content.filter((u) => u.status === 'ACTIVE'));
    });
  }

  invalid(ctrl: string): boolean {
    const control = this.form.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Please correct the highlighted fields.');
      return;
    }
    this.saving.set(true);
    const formValue = this.form.getRawValue();
    const body: EmitNotificationRequest = {
      employeeId: formValue.employeeId!,
      category: formValue.category as EmitNotificationRequest['category'],
      message: formValue.message!,
    };
    this.notifications.emit(body).subscribe({
      next: (delivered) => {
        this.lastDelivered.set(delivered);
        this.toast.success(`Notification delivered to ${delivered.employeeId}.`);
        this.saving.set(false);
        this.form.reset({ employeeId: '', category: 'SURVEY', message: '' });
      },
      error: () => this.saving.set(false),
    });
  }

  recipientLabel(user: UserResponse): string {
    return `${user.employeeId} — ${user.name} (${user.email})`;
  }

  back(): void {
    this.router.navigate(['/notifications']);
  }
}
