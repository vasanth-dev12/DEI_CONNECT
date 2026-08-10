import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { SurveyService } from '../../../core/services/survey.service';
import { ToastService } from '../../../core/services/toast.service';
import { CreateQuestionRequest, QuestionResponse, SurveyResponse, UpdateQuestionRequest } from '../../../core/models/survey.model';
import { ALL_QUESTION_TYPES, ALL_SURVEY_DIMENSIONS, QuestionType, SurveyDimension } from '../../../core/models/enums';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

const ANSWER_TYPE_HINTS: Record<QuestionType, string> = {
  LIKERT_SCALE: 'Employees pick a number from 1 (strongly disagree) to 5 (strongly agree).',
  BINARY: 'Employees pick Yes or No.',
};

@Component({
  selector: 'app-survey-questions',
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink, PageHeaderComponent, EmptyStateComponent,
    ConfirmModalComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './survey-questions.component.html',
})
export class SurveyQuestionsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly surveys = inject(SurveyService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);

  readonly id = input.required<string>();

  readonly questionTypes = ALL_QUESTION_TYPES;
  readonly dimensions = ALL_SURVEY_DIMENSIONS;

  readonly survey = signal<SurveyResponse | null>(null);
  readonly questions = signal<QuestionResponse[]>([]);
  readonly editingId = signal<number | null>(null);
  readonly toDelete = signal<QuestionResponse | null>(null);
  readonly saving = signal(false);

  readonly orderedQuestions = computed(() => [...this.questions()].sort((a, b) => a.sequenceOrder - b.sequenceOrder));

  readonly form = this.fb.group({
    questionText: ['', [Validators.required]],
    questionType: ['LIKERT_SCALE', [Validators.required]],
    dimension: ['BELONGING', [Validators.required]],
    mandatory: [true],
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.surveys.getById(Number(this.id())).subscribe((survey) => this.survey.set(survey));
    this.surveys.listQuestions(Number(this.id())).subscribe((questions) => this.questions.set(questions));
  }

  invalid(ctrl: string): boolean {
    const control = this.form.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  answerTypeHint(): string {
    return ANSWER_TYPE_HINTS[this.form.getRawValue().questionType as QuestionType] ?? '';
  }

  canManageQuestion(question: QuestionResponse): boolean {
    if (this.auth.role() !== 'DEI_MANAGER') return false;
    return question.creatorManagerId === this.auth.currentUser()?.userId;
  }

  edit(question: QuestionResponse): void {
    this.editingId.set(question.questionId);
    this.form.patchValue({
      questionText: question.questionText,
      questionType: question.questionType,
      dimension: question.dimension,
      mandatory: question.mandatory,
    });
  }

  resetForm(): void {
    this.editingId.set(null);
    this.form.reset({
      questionText: '',
      questionType: 'LIKERT_SCALE',
      dimension: 'BELONGING',
      mandatory: true,
    });
  }

  save(): void {
    const formValue = this.form.getRawValue();

    const questionText = (formValue.questionText ?? '').trim();
    if (!questionText) {
      this.form.get('questionText')?.markAsTouched();
      this.toast.error('Please enter the question text.');
      return;
    }

    const questionType = (formValue.questionType ?? 'LIKERT_SCALE') as QuestionType;
    const dimension = (formValue.dimension ?? 'BELONGING') as SurveyDimension;
    const mandatory = formValue.mandatory ?? false;

    this.saving.set(true);
    const surveyId = Number(this.id());
    const editingId = this.editingId();

    if (editingId) {
      const body: UpdateQuestionRequest = { questionText, questionType, dimension, mandatory };
      this.surveys.updateQuestion(surveyId, editingId, body).subscribe({
        next: () => {
          this.toast.success('Question updated.');
          this.saving.set(false);
          this.resetForm();
          this.reload();
        },
        error: () => this.saving.set(false),
      });
    } else {
      const body: CreateQuestionRequest = { questionText, questionType, dimension, mandatory };
      this.surveys.addQuestion(surveyId, body).subscribe({
        next: () => {
          this.toast.success('Question added.');
          this.saving.set(false);
          this.resetForm();
          this.reload();
        },
        error: () => this.saving.set(false),
      });
    }
  }

  askDelete(question: QuestionResponse): void {
    this.toDelete.set(question);
  }

  confirmDelete(): void {
    const question = this.toDelete();
    if (!question) return;
    this.surveys.deleteQuestion(Number(this.id()), question.questionId).subscribe(() => {
      this.toast.success('Question deleted.');
      this.toDelete.set(null);
      this.reload();
    });
  }
}
