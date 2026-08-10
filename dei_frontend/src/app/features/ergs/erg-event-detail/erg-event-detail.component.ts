import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { ErgService } from '../../../core/services/erg.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { EventResponse, ParticipantResponse } from '../../../core/models/erg.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-erg-event-detail',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, RouterLink, PageHeaderComponent, EmptyStateComponent,
    StatusBadgeComponent, ConfirmModalComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './erg-event-detail.component.html',
})
export class ErgEventDetailComponent implements OnInit {
  private readonly ergs = inject(ErgService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input.required<string>();
  readonly eventId = input.required<string>();

  readonly event = signal<EventResponse | null>(null);
  readonly participants = signal<ParticipantResponse[] | null>(null);
  readonly participationBusy = signal(false);
  readonly confirmingDelete = signal(false);

  ngOnInit(): void {
    this.ergs.getEvent(Number(this.id()), Number(this.eventId())).subscribe((eventResponse) => this.event.set(eventResponse));
    if (this.auth.hasAnyRole(['DEI_MANAGER', 'ERG_LEAD', 'ADMIN'])) {
      this.ergs.listParticipants(Number(this.id()), Number(this.eventId())).subscribe({
        next: (participantList) => this.participants.set(participantList),
        error: () => this.participants.set(null),
      });
    }
  }

  participate(): void {
    this.participationBusy.set(true);
    this.ergs.participate(Number(this.id()), Number(this.eventId())).subscribe({
      next: () => {
        this.toast.success('You are registered for this event.');
        this.participationBusy.set(false);
      },
      error: () => this.participationBusy.set(false),
    });
  }

  cancelParticipation(): void {
    this.participationBusy.set(true);
    this.ergs.cancelParticipation(Number(this.id()), Number(this.eventId())).subscribe({
      next: () => {
        this.toast.success('Your participation was cancelled.');
        this.participationBusy.set(false);
      },
      error: () => this.participationBusy.set(false),
    });
  }

  askDelete(): void {
    this.confirmingDelete.set(true);
  }

  confirmDelete(): void {
    this.ergs.deleteEvent(Number(this.id()), Number(this.eventId())).subscribe(() => {
      this.toast.success('Event deleted.');
      this.confirmingDelete.set(false);
      this.router.navigate(['/ergs', this.id()]);
    });
  }
}
