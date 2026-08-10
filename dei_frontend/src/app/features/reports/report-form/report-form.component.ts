import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { ReportService } from '../../../core/services/report.service';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../core/services/toast.service';
import { DEIReportRequest } from '../../../core/models/reporting.model';
import { ScopeValueOption } from '../../../core/models/iam.model';
import { ALL_REPORT_METRICS, ALL_REPORT_SCOPES, ReportMetric } from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-report-form',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './report-form.component.html',
})
export class ReportFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reports = inject(ReportService);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input<string>();
  readonly isEdit = computed(() => !!this.id());
  readonly saving = signal(false);

  readonly scopes = ALL_REPORT_SCOPES;
  readonly metrics = ALL_REPORT_METRICS;

  readonly selectedMetrics = signal<Set<ReportMetric>>(new Set());
  readonly metricsValid = computed(() => this.selectedMetrics().size > 0);
  readonly metricsTouched = signal(false);

  readonly currentScope = signal<string>('ORGANISATION');
  readonly deptOptions = signal<ScopeValueOption[]>([]);
  readonly gradeOptions = signal<ScopeValueOption[]>([]);

  readonly form = this.fb.group({
    scope: ['ORGANISATION', [Validators.required]],
    scopeValue: [''],
  });

  ngOnInit(): void {
    this.users.scopeValues('DEPARTMENT').subscribe((options) => this.deptOptions.set(options));
    this.users.scopeValues('GRADE').subscribe((options) => this.gradeOptions.set(options));

    this.form.get('scope')!.valueChanges.subscribe((scope) => {
      this.currentScope.set(scope ?? 'ORGANISATION');
      this.applyScopeValidators(scope ?? 'ORGANISATION');
      this.form.get('scopeValue')!.setValue('');
    });
    this.applyScopeValidators('ORGANISATION');

    if (this.isEdit()) {
      this.reports.getById(Number(this.id())).subscribe((response) => {
        this.form.get('scope')!.setValue(response.scope, { emitEvent: false });
        this.currentScope.set(response.scope);
        this.applyScopeValidators(response.scope);
        this.form.get('scopeValue')!.setValue(response.scopeValue ?? '');
        this.selectedMetrics.set(new Set(response.metrics));
      });
    }
  }

  private applyScopeValidators(scope: string): void {
    const scopeValueControl = this.form.get('scopeValue')!;
    if (scope === 'DEPARTMENT' || scope === 'GRADE') {
      scopeValueControl.setValidators([Validators.required]);
    } else {
      scopeValueControl.clearValidators();
    }
    scopeValueControl.updateValueAndValidity({ emitEvent: false });
  }

  invalid(ctrl: string): boolean {
    const control = this.form.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  toggleMetric(metric: ReportMetric, event: Event): void {
    this.metricsTouched.set(true);
    const checked = (event.target as HTMLInputElement).checked;
    this.selectedMetrics.update((currentMetrics) => {
      const updatedMetrics = new Set(currentMetrics);
      if (checked) {
        updatedMetrics.add(metric);
      } else {
        updatedMetrics.delete(metric);
      }
      return updatedMetrics;
    });
  }

  save(): void {
    this.metricsTouched.set(true);
    if (this.form.invalid || !this.metricsValid()) {
      this.form.markAllAsTouched();
      this.toast.error('Please correct the highlighted fields.');
      return;
    }
    this.saving.set(true);
    const formValue = this.form.getRawValue();
    const body: DEIReportRequest = {
      scope: formValue.scope as DEIReportRequest['scope'],
      scopeValue: formValue.scopeValue || null,
      metrics: Array.from(this.selectedMetrics()),
    };

    if (this.isEdit()) {
      this.reports.update(Number(this.id()), body).subscribe({
        next: () => { this.toast.success('Report updated.'); this.router.navigate(['/reports']); },
        error: () => this.saving.set(false),
      });
    } else {
      this.reports.create(body).subscribe({
        next: () => { this.toast.success('Report created.'); this.router.navigate(['/reports']); },
        error: () => this.saving.set(false),
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/reports']);
  }
}
