import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { ErgService } from '../../../core/services/erg.service';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../core/services/toast.service';
import { CreateErgRequest, UpdateErgRequest } from '../../../core/models/erg.model';
import { UserResponse } from '../../../core/models/iam.model';
import { ALL_ERG_FOCUSES, ALL_ERG_STATUSES } from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-erg-form',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './erg-form.component.html',
})
export class ErgFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly ergs = inject(ErgService);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input<string>();
  readonly isEdit = computed(() => !!this.id());
  readonly saving = signal(false);

  readonly focuses = ALL_ERG_FOCUSES;
  readonly statuses = ALL_ERG_STATUSES;

  readonly ergLeads = signal<UserResponse[]>([]);
  readonly executives = signal<UserResponse[]>([]);

  readonly form = this.fb.group({
    ergName: ['', [Validators.required, Validators.maxLength(150)]],
    focus: ['GENDER', [Validators.required]],
    mission: [''],
    ergLeadId: [null as number | null, [Validators.required]],
    executiveSponsorId: [null as number | null],
    foundedDate: [''],
    status: ['ACTIVE'],
  });

  ngOnInit(): void {
    this.users.listByRole('ERG_LEAD').subscribe((pagedResult) => this.ergLeads.set(pagedResult.content));
    this.users.listByRole('EXECUTIVE').subscribe((pagedResult) => this.executives.set(pagedResult.content));

    if (this.isEdit()) {
      this.ergs.getById(Number(this.id())).subscribe((ergResponse) => {
        this.form.patchValue({
          ergName: ergResponse.ergName,
          focus: ergResponse.focus,
          mission: ergResponse.mission ?? '',
          ergLeadId: ergResponse.ergLeadId,
          executiveSponsorId: ergResponse.executiveSponsorId,
          foundedDate: ergResponse.foundedDate ?? '',
          status: ergResponse.status,
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
      const body: UpdateErgRequest = {
        ergName: formValue.ergName!,
        focus: formValue.focus as UpdateErgRequest['focus'],
        mission: formValue.mission || null,
        ergLeadId: formValue.ergLeadId!,
        executiveSponsorId: formValue.executiveSponsorId,
        foundedDate: formValue.foundedDate || null,
        status: formValue.status as UpdateErgRequest['status'],
      };
      this.ergs.update(Number(this.id()), body).subscribe({
        next: () => { this.toast.success('ERG updated.'); this.router.navigate(['/ergs']); },
        error: () => this.saving.set(false),
      });
    } else {
      const body: CreateErgRequest = {
        ergName: formValue.ergName!,
        focus: formValue.focus as CreateErgRequest['focus'],
        mission: formValue.mission || null,
        ergLeadId: formValue.ergLeadId!,
        executiveSponsorId: formValue.executiveSponsorId,
        foundedDate: formValue.foundedDate || null,
      };
      this.ergs.create(body).subscribe({
        next: () => { this.toast.success('ERG created.'); this.router.navigate(['/ergs']); },
        error: () => this.saving.set(false),
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/ergs']);
  }
}
