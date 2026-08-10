import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { SurveyService } from '../../../core/services/survey.service';
import { ToastService } from '../../../core/services/toast.service';
import { AnswerItem, QuestionResponse, SurveyResponse, SubmitSurveyRequest } from '../../../core/models/survey.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-survey-respond',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, PageHeaderComponent, EmptyStateComponent],
  templateUrl: './survey-respond.component.html',
})
export class SurveyRespondComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly surveys = inject(SurveyService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input.required<string>();
  readonly saving = signal(false);

  readonly survey = signal<SurveyResponse | null>(null);
  readonly orderedQuestions = computed<QuestionResponse[]>(() => {
    const survey = this.survey();
    if (!survey?.questions) return [];
    return [...survey.questions].sort((a, b) => a.sequenceOrder - b.sequenceOrder);
  });

  readonly likertValues = [1, 2, 3, 4, 5];

  readonly form = this.fb.group({
    answers: this.fb.array<FormGroup>([]),
  });

  get answers(): FormArray<FormGroup> {
    return this.form.get('answers') as FormArray<FormGroup>;
  }

  ngOnInit(): void {
    this.surveys.getById(Number(this.id())).subscribe((survey) => {
      this.survey.set(survey);
      const ordered = [...(survey.questions ?? [])].sort((a, b) => a.sequenceOrder - b.sequenceOrder);
      const groups: FormGroup[] = ordered.map((question) =>
        this.fb.group({
          questionId: [question.questionId],
          numericValue: [null as number | null, question.mandatory ? [Validators.required] : []],
        }),
      );
      this.form.setControl('answers', this.fb.array(groups));
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Please answer all mandatory questions.');
      return;
    }
    this.saving.set(true);
    const rawAnswers = this.answers.getRawValue() as { questionId: number; numericValue: number | null }[];
    const body: SubmitSurveyRequest = {
      answers: rawAnswers
        .filter((answer) => answer.numericValue !== null && answer.numericValue !== undefined)
        .map((answer): AnswerItem => ({ questionId: answer.questionId, numericValue: answer.numericValue })),
    };
    this.surveys.submit(Number(this.id()), body).subscribe({
      next: (acknowledgement) => {
        this.toast.success(acknowledgement.message || 'Response submitted. Thank you.');
        this.router.navigate(['/surveys']);
      },
      error: () => this.saving.set(false),
    });
  }
}
