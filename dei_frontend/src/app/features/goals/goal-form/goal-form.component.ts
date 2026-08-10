import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/auth/auth.service';
import { GoalService } from '../../../core/services/goal.service';
import { ToastService } from '../../../core/services/toast.service';
import { CreateGoalRequest, UpdateGoalRequest } from '../../../core/models/goal.model';
import { ALL_GOAL_DIMENSIONS, ALL_GOAL_STATUSES } from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-goal-form',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './goal-form.component.html',
})
export class GoalFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly goals = inject(GoalService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly id = input<string>();
  readonly isEdit = computed(() => !!this.id());
  readonly saving = signal(false);

  readonly dimensions = ALL_GOAL_DIMENSIONS;
  readonly statuses = ALL_GOAL_STATUSES;

  private readonly loadedOwnerName = signal<string | null>(null);
  readonly ownerName = computed(() => {
    if (this.isEdit()) {
      return this.loadedOwnerName() ?? '—';
    }
    const me = this.auth.currentUser();
    return me ? `${me.name} (${me.employeeId})` : '—';
  });

  readonly form = this.fb.group({
    goalName: ['', [Validators.required, Validators.maxLength(200)]],
    dimension: ['GENDER', [Validators.required]],
    targetGroup: [''],
    baselineValue: [null as number | null, [Validators.required, Validators.min(0)]],
    targetValue: [null as number | null, [Validators.required, Validators.min(0)]],
    targetYear: [null as number | null, [Validators.required, Validators.min(1)]],
    status: ['ACTIVE'],
  });

  ngOnInit(): void {
    if (this.isEdit()) {
      this.goals.getById(Number(this.id())).subscribe((goal) => {
        this.loadedOwnerName.set(goal.ownerName ?? null);
        this.form.patchValue({
          goalName: goal.goalName,
          dimension: goal.dimension,
          targetGroup: goal.targetGroup ?? '',
          baselineValue: goal.baselineValue,
          targetValue: goal.targetValue,
          targetYear: goal.targetYear,
          status: goal.status,
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
      const body: UpdateGoalRequest = {
        goalName: formValue.goalName!,
        dimension: formValue.dimension as UpdateGoalRequest['dimension'],
        targetGroup: formValue.targetGroup || null,
        baselineValue: formValue.baselineValue!,
        targetValue: formValue.targetValue!,
        targetYear: formValue.targetYear!,
        status: formValue.status as UpdateGoalRequest['status'],
      };
      this.goals.update(Number(this.id()), body).subscribe({
        next: () => { this.toast.success('Goal updated.'); this.router.navigate(['/goals']); },
        error: () => this.saving.set(false),
      });
    } else {
      const body: CreateGoalRequest = {
        goalName: formValue.goalName!,
        dimension: formValue.dimension as CreateGoalRequest['dimension'],
        targetGroup: formValue.targetGroup || null,
        baselineValue: formValue.baselineValue!,
        targetValue: formValue.targetValue!,
        targetYear: formValue.targetYear!,
      };
      this.goals.create(body).subscribe({
        next: () => { this.toast.success('Goal created.'); this.router.navigate(['/goals']); },
        error: () => this.saving.set(false),
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/goals']);
  }
}
