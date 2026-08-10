import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { SurveyService } from '../../../core/services/survey.service';
import { ToastService } from '../../../core/services/toast.service';
import { CreateSurveyRequest, UpdateSurveyRequest } from '../../../core/models/survey.model';
import { ALL_SURVEY_TYPES } from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

function notBeforeToday(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;
    const today = new Date().toISOString().slice(0, 10);
    return value < today ? { pastDate: true } : null;
  };
}

function closeAfterLaunch(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const launch = group.get('launchDate')?.value;
    const close = group.get('closeDate')?.value;
    if (!launch || !close) return null;
    return close < launch ? { closeBeforeLaunch: true } : null;
  };
}

@Component({
  selector: 'app-survey-form',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './survey-form.component.html',
})
export class SurveyFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly surveys = inject(SurveyService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input<string>();
  readonly isEdit = computed(() => !!this.id());
  readonly saving = signal(false);

  readonly surveyTypes = ALL_SURVEY_TYPES;
  readonly today = new Date().toISOString().slice(0, 10);

  readonly form = this.fb.group(
    {
      surveyName: ['', [Validators.required, Validators.maxLength(200)]],
      surveyType: ['ANNUAL', [Validators.required]],
      launchDate: [null as string | null, [notBeforeToday()]],
      closeDate: [null as string | null, [notBeforeToday()]],
      anonymised: [true],
      minResponseThreshold: [1, [Validators.required, Validators.min(1)]],
    },
    { validators: closeAfterLaunch() },
  );

  ngOnInit(): void {
    if (this.isEdit()) {
      this.surveys.getById(Number(this.id())).subscribe((survey) => {
        this.form.patchValue({
          surveyName: survey.surveyName,
          surveyType: survey.surveyType,
          launchDate: survey.launchDate,
          closeDate: survey.closeDate,
          anonymised: survey.anonymised ?? true,
          minResponseThreshold: survey.minResponseThreshold,
        });
      });
    }
  }

  invalid(ctrl: string): boolean {
    const control = this.form.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  dateRangeInvalid(): boolean {
    const control = this.form.get('closeDate');
    return this.form.hasError('closeBeforeLaunch') && !!control && (control.touched || control.dirty);
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
      const body: UpdateSurveyRequest = {
        surveyName: formValue.surveyName!,
        surveyType: formValue.surveyType as UpdateSurveyRequest['surveyType'],
        launchDate: formValue.launchDate,
        closeDate: formValue.closeDate,
        anonymised: formValue.anonymised,
        minResponseThreshold: formValue.minResponseThreshold!,
      };
      this.surveys.update(Number(this.id()), body).subscribe({
        next: () => { this.toast.success('Survey updated.'); this.router.navigate(['/surveys']); },
        error: () => this.saving.set(false),
      });
    } else {
      const body: CreateSurveyRequest = {
        surveyName: formValue.surveyName!,
        surveyType: formValue.surveyType as CreateSurveyRequest['surveyType'],
        launchDate: formValue.launchDate,
        closeDate: formValue.closeDate,
        anonymised: formValue.anonymised,
        minResponseThreshold: formValue.minResponseThreshold!,
        questions: [],
      };
      this.surveys.create(body).subscribe({
        next: () => { this.toast.success('Survey created.'); this.router.navigate(['/surveys']); },
        error: () => this.saving.set(false),
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/surveys']);
  }
}
