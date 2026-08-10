import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { ErgService } from '../../../core/services/erg.service';
import { ToastService } from '../../../core/services/toast.service';
import { CreateEventRequest, UpdateEventRequest } from '../../../core/models/erg.model';
import { ALL_EVENT_STATUSES, ALL_EVENT_TYPES } from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-erg-event-form',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './erg-event-form.component.html',
})
export class ErgEventFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly ergs = inject(ErgService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input.required<string>();
  readonly eventId = input<string>();
  readonly isEdit = computed(() => !!this.eventId());
  readonly saving = signal(false);

  readonly eventTypes = ALL_EVENT_TYPES;
  readonly eventStatuses = ALL_EVENT_STATUSES;

  readonly form = this.fb.group({
    eventName: ['', [Validators.required, Validators.maxLength(150)]],
    eventType: ['NETWORKING_SESSION', [Validators.required]],
    date: ['', [Validators.required]],
    attendeeCount: [null as number | null, [Validators.min(0)]],
    budgetSpent: [null as number | null, [Validators.min(0)]],
    status: ['PLANNED'],
  });

  ngOnInit(): void {
    if (this.isEdit()) {
      this.ergs.getEvent(Number(this.id()), Number(this.eventId())).subscribe((eventResponse) => {
        this.form.patchValue({
          eventName: eventResponse.eventName,
          eventType: eventResponse.eventType,
          date: eventResponse.date,
          attendeeCount: eventResponse.attendeeCount,
          budgetSpent: eventResponse.budgetSpent,
          status: eventResponse.status,
        });
      });
    }
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

    if (this.isEdit()) {
      const body: UpdateEventRequest = {
        eventName: formValue.eventName!,
        eventType: formValue.eventType as UpdateEventRequest['eventType'],
        date: formValue.date!,
        attendeeCount: formValue.attendeeCount,
        budgetSpent: formValue.budgetSpent,
        status: formValue.status as UpdateEventRequest['status'],
      };
      this.ergs.updateEvent(Number(this.id()), Number(this.eventId()), body).subscribe({
        next: () => { this.toast.success('Event updated.'); this.router.navigate(['/ergs', this.id()]); },
        error: () => this.saving.set(false),
      });
    } else {
      const body: CreateEventRequest = {
        eventName: formValue.eventName!,
        eventType: formValue.eventType as CreateEventRequest['eventType'],
        date: formValue.date!,
        attendeeCount: formValue.attendeeCount,
        budgetSpent: formValue.budgetSpent,
      };
      this.ergs.createEvent(Number(this.id()), body).subscribe({
        next: () => { this.toast.success('Event created.'); this.router.navigate(['/ergs', this.id()]); },
        error: () => this.saving.set(false),
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/ergs', this.id()]);
  }
}
