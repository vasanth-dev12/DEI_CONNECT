import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { PayEquityService } from '../../../core/services/pay-equity.service';
import { ToastService } from '../../../core/services/toast.service';
import { PayEquityAnalysisRequest } from '../../../core/models/pay-equity.model';
import { ALL_CONTROL_VARIABLES, ALL_PAY_DIMENSIONS, ControlVariable } from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-analysis-form',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './analysis-form.component.html',
})
export class AnalysisFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly payEquity = inject(PayEquityService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input<string>();
  readonly isEdit = computed(() => !!this.id());
  readonly saving = signal(false);
  readonly touchedControlVariables = signal(false);

  readonly dimensions = ALL_PAY_DIMENSIONS;
  readonly controlVariables = ALL_CONTROL_VARIABLES;

  readonly selectedControlVariables = signal<ControlVariable[]>([]);
  readonly controlVariablesInvalid = computed(
    () => this.touchedControlVariables() && this.selectedControlVariables().length === 0,
  );

  readonly form = this.fb.group({
    analysisPeriod: ['', [Validators.required]],
    dimension: ['GENDER', [Validators.required]],
  });

  ngOnInit(): void {
    if (this.isEdit()) {
      this.payEquity.getAnalysis(Number(this.id())).subscribe((analysis) => {
        this.form.patchValue({
          analysisPeriod: analysis.analysisPeriod,
          dimension: analysis.dimension,
        });
        this.selectedControlVariables.set([...analysis.controlVariables]);
      });
    }
  }

  isSelected(variable: ControlVariable): boolean {
    return this.selectedControlVariables().includes(variable);
  }

  toggleControlVariable(variable: ControlVariable): void {
    this.touchedControlVariables.set(true);
    const current = this.selectedControlVariables();
    this.selectedControlVariables.set(
      current.includes(variable) ? current.filter((existing) => existing !== variable) : [...current, variable],
    );
  }

  invalid(ctrl: string): boolean {
    const control = this.form.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  save(): void {
    this.touchedControlVariables.set(true);
    if (this.form.invalid || this.selectedControlVariables().length === 0) {
      this.form.markAllAsTouched();
      this.toast.error('Please correct the highlighted fields.');
      return;
    }
    this.saving.set(true);
    const formValue = this.form.getRawValue();
    const body: PayEquityAnalysisRequest = {
      analysisPeriod: formValue.analysisPeriod!,
      dimension: formValue.dimension as PayEquityAnalysisRequest['dimension'],
      controlVariables: this.selectedControlVariables(),
    };

    if (this.isEdit()) {
      this.payEquity.updateAnalysis(Number(this.id()), body).subscribe({
        next: (analysis) => { this.toast.success('Analysis updated.'); this.router.navigate(['/pay-equity/analyses', analysis.id]); },
        error: () => this.saving.set(false),
      });
    } else {
      this.payEquity.createAnalysis(body).subscribe({
        next: (analysis) => { this.toast.success('Analysis created.'); this.router.navigate(['/pay-equity/analyses', analysis.id]); },
        error: () => this.saving.set(false),
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/pay-equity/analyses']);
  }
}
