import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { GoalService } from '../../../core/services/goal.service';
import { ToastService } from '../../../core/services/toast.service';
import { CreateProgressRequest, GoalResponse, ProgressResponse, UpdateProgressRequest } from '../../../core/models/goal.model';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-goal-detail',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink, PageHeaderComponent, PaginatorComponent,
    EmptyStateComponent, StatusBadgeComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './goal-detail.component.html',
})
export class GoalDetailComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly goals = inject(GoalService);
  private readonly toast = inject(ToastService);

  readonly id = input.required<string>();
  readonly goal = signal<GoalResponse | null>(null);
  readonly progressPage = signal<Page<ProgressResponse> | null>(null);
  readonly editingId = signal<number | null>(null);
  readonly savingAdd = signal(false);
  readonly savingEdit = signal(false);

  private currentProgressPage = 0;

  readonly addForm = this.fb.group({
    period: ['', [Validators.required]],
    actualValue: [null as number | null, [Validators.required]],
    commentary: [''],
  });

  readonly editForm = this.fb.group({
    period: ['', [Validators.required]],
    actualValue: [null as number | null, [Validators.required]],
    commentary: [''],
  });

  ngOnInit(): void {
    this.goals.getById(Number(this.id())).subscribe((goal) => this.goal.set(goal));
    this.reloadProgress(0);
  }

  reloadProgress(pageIndex: number): void {
    this.currentProgressPage = pageIndex;
    this.goals
      .listProgress(Number(this.id()), { page: pageIndex, size: 10, sort: 'period,desc' })
      .subscribe((pagedResult) => this.progressPage.set(pagedResult));
  }

  addInvalid(ctrl: string): boolean {
    const control = this.addForm.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  editInvalid(ctrl: string): boolean {
    const control = this.editForm.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  addProgress(): void {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      this.toast.error('Please correct the highlighted fields.');
      return;
    }
    this.savingAdd.set(true);
    const formValue = this.addForm.getRawValue();
    const body: CreateProgressRequest = {
      period: formValue.period!,
      actualValue: formValue.actualValue!,
      commentary: formValue.commentary || null,
    };
    this.goals.addProgress(Number(this.id()), body).subscribe({
      next: () => {
        this.toast.success('Progress added.');
        this.savingAdd.set(false);
        this.addForm.reset();
        this.reloadProgress(0);
      },
      error: () => this.savingAdd.set(false),
    });
  }

  startEdit(progress: ProgressResponse): void {
    this.editingId.set(progress.progressId);
    this.editForm.reset({
      period: progress.period,
      actualValue: progress.actualValue,
      commentary: progress.commentary ?? '',
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  saveEdit(progress: ProgressResponse): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      this.toast.error('Please correct the highlighted fields.');
      return;
    }
    this.savingEdit.set(true);
    const formValue = this.editForm.getRawValue();
    const body: UpdateProgressRequest = {
      period: formValue.period!,
      actualValue: formValue.actualValue!,
      commentary: formValue.commentary || null,
    };
    this.goals.updateProgress(Number(this.id()), progress.progressId, body).subscribe({
      next: () => {
        this.toast.success('Progress updated.');
        this.savingEdit.set(false);
        this.editingId.set(null);
        this.reloadProgress(this.currentProgressPage);
      },
      error: () => this.savingEdit.set(false),
    });
  }

  confirmEntry(progress: ProgressResponse): void {
    this.goals.confirmProgress(Number(this.id()), progress.progressId).subscribe(() => {
      this.toast.success('Progress confirmed.');
      this.reloadProgress(this.currentProgressPage);
    });
  }
}
