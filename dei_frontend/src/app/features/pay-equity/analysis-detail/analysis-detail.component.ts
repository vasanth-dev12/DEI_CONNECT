import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PayEquityService } from '../../../core/services/pay-equity.service';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  PayEquityAnalysisResponse,
  PayGapFlagResponse,
  UpdatePayGapFlagRequest,
} from '../../../core/models/pay-equity.model';
import { ALL_FLAG_STATUSES } from '../../../core/models/enums';
import { UserResponse } from '../../../core/models/iam.model';
import { humanize } from '../../../core/constants/labels';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-analysis-detail',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink, PageHeaderComponent, EmptyStateComponent,
    StatusBadgeComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './analysis-detail.component.html',
})
export class AnalysisDetailComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly payEquity = inject(PayEquityService);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);

  readonly id = input.required<string>();

  readonly analysis = signal<PayEquityAnalysisResponse | null>(null);
  readonly flags = signal<PayGapFlagResponse[] | null>(null);
  readonly owners = signal<UserResponse[]>([]);

  readonly flagStatuses = ALL_FLAG_STATUSES;

  readonly remediatingId = signal<number | null>(null);
  readonly savingRemediate = signal(false);
  readonly computing = signal(false);

  readonly remediateForm = this.fb.group({
    remediationOwnerId: [null as number | null, [Validators.required]],
    status: ['REMEDIATION_IN_PROGRESS', [Validators.required]],
  });

  ngOnInit(): void {
    this.payEquity.getAnalysis(Number(this.id())).subscribe((analysis) => this.analysis.set(analysis));
    this.users.listByRole('EMPLOYEE', 200).subscribe((pagedResult) => this.owners.set(pagedResult.content));
    this.reloadFlags();
  }

  reloadFlags(): void {
    this.payEquity.listFlags(Number(this.id())).subscribe((flags) => this.flags.set(flags));
  }

  controlVariablesLabel(analysis: PayEquityAnalysisResponse): string {
    return analysis.controlVariables.map((variable) => humanize(variable)).join(', ') || '—';
  }

  publish(): void {
    this.payEquity.publishAnalysis(Number(this.id())).subscribe((analysis) => {
      this.analysis.set(analysis);
      this.toast.success('Analysis published.');
    });
  }

  compute(): void {
    this.computing.set(true);
    this.payEquity.computeAnalysis(Number(this.id())).subscribe({
      next: (analysis) => {
        this.analysis.set(analysis);
        this.computing.set(false);
        this.reloadFlags();
        this.toast.success('Analysis computed from workforce data.');
      },
      error: () => this.computing.set(false),
    });
  }

  startRemediate(flag: PayGapFlagResponse): void {
    this.remediatingId.set(flag.id);
    this.remediateForm.reset({
      remediationOwnerId: flag.remediationOwnerId,
      status: flag.status === 'OPEN' ? 'REMEDIATION_IN_PROGRESS' : flag.status,
    });
  }

  cancelRemediate(): void {
    this.remediatingId.set(null);
  }

  remediateInvalid(ctrl: string): boolean {
    const control = this.remediateForm.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  saveRemediate(flag: PayGapFlagResponse): void {
    if (this.remediateForm.invalid) {
      this.remediateForm.markAllAsTouched();
      this.toast.error('Please correct the highlighted fields.');
      return;
    }
    this.savingRemediate.set(true);
    const formValue = this.remediateForm.getRawValue();
    const body: UpdatePayGapFlagRequest = {
      remediationOwnerId: formValue.remediationOwnerId!,
      status: formValue.status as UpdatePayGapFlagRequest['status'],
    };
    this.payEquity.updateFlag(Number(this.id()), flag.id, body).subscribe({
      next: () => {
        this.toast.success('Flag updated.');
        this.savingRemediate.set(false);
        this.remediatingId.set(null);
        this.reloadFlags();
      },
      error: () => this.savingRemediate.set(false),
    });
  }
}
