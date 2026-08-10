import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { ErgService } from '../../../core/services/erg.service';
import { ErgResponse, EventResponse } from '../../../core/models/erg.model';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-erg-lead-dashboard',
  standalone: true,
  imports: [DatePipe, StatCardComponent, EmptyStateComponent, StatusBadgeComponent, EnumLabelPipe],
  templateUrl: './erg-lead-dashboard.component.html',
})
export class ErgLeadDashboardComponent implements OnInit {
  private readonly ergs = inject(ErgService);
  private readonly auth = inject(AuthService);

  readonly myErgs = signal<ErgResponse[]>([]);
  readonly events = signal<EventResponse[]>([]);
  readonly totalMembers = signal(0);

  ngOnInit(): void {
    this.ergs.list({ page: 0, size: 50 }).subscribe((pagedResult) => {
      const userId = this.auth.currentUser()?.userId;
      const myErgList = pagedResult.content.filter((erg) => erg.ergLeadId === userId);
      this.myErgs.set(myErgList);
      this.totalMembers.set(myErgList.reduce((sum, erg) => sum + erg.memberCount, 0));

      if (myErgList.length === 0) {
        this.events.set([]);
        return;
      }

      forkJoin(
        myErgList.map((erg) => this.ergs.listEvents(erg.ergId, { page: 0, size: 50 })),
      ).subscribe((eventPages) => {
        const today = new Date().toISOString().slice(0, 10);
        const upcomingEvents = eventPages
          .flatMap((eventsPage) => eventsPage.content)
          .filter((event) => event.status === 'PLANNED' && event.date >= today)
          .sort((a, b) => a.date.localeCompare(b.date));
        this.events.set(upcomingEvents);
      });
    });
  }
}
