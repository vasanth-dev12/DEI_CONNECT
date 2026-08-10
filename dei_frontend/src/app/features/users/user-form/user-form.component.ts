import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../core/services/toast.service';
import { AdminCreateUserRequest, AdminUpdateUserRequest, UserResponse } from '../../../core/models/iam.model';
import { ALL_DEPARTMENTS, ALL_ROLES, UserStatus } from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { RoleLabelPipe } from '../../../shared/pipes/role-label.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, RoleLabelPipe, EnumLabelPipe],
  templateUrl: './user-form.component.html',
})
export class UserFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly id = input<string>();
  readonly isEdit = computed(() => !!this.id());
  readonly saving = signal(false);

  readonly roles = ALL_ROLES;
  readonly departments = ALL_DEPARTMENTS;
  readonly statuses: UserStatus[] = ['ACTIVE', 'INACTIVE'];

  readonly managers = signal<UserResponse[]>([]);
  readonly hrPartners = signal<UserResponse[]>([]);
  readonly isEmployeeRole = signal(false);

  readonly form = this.fb.group({
    employeeId: ['', [Validators.required]],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['EMPLOYEE', [Validators.required]],
    departmentName: ['SOFTWARE_ENGINEERING', [Validators.required]],
    gradeId: [null as number | null, [Validators.min(0)]],
    status: ['ACTIVE'],
    managerId: [null as number | null],
    hrId: [null as number | null],
    salary: [null as number | null, [Validators.required, Validators.min(0)]],
    yearsOfExperience: [null as number | null, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.users.listByRole('DEI_MANAGER').subscribe((pagedResult) => this.managers.set(pagedResult.content));
    this.users.listByRole('HR_BIZ_PARTNER').subscribe((pagedResult) => this.hrPartners.set(pagedResult.content));
    this.form.get('role')!.valueChanges.subscribe((role) => this.applyRoleRules(role));
    this.applyRoleRules(this.form.get('role')!.value);

    if (this.isEdit()) {
      this.form.get('employeeId')!.clearValidators();
      this.form.get('password')!.clearValidators();
      this.form.get('employeeId')!.updateValueAndValidity();
      this.form.get('password')!.updateValueAndValidity();

      this.users.getById(Number(this.id())).subscribe((user) => {
        this.form.patchValue({
          employeeId: user.employeeId,
          name: user.name,
          email: user.email,
          role: user.role,
          departmentName: user.departmentName ?? 'SOFTWARE_ENGINEERING',
          gradeId: user.gradeId,
          status: user.status,
          managerId: user.managerId,
          hrId: user.hrId,
          salary: user.salary,
          yearsOfExperience: user.yearsOfExperience,
        });
        this.applyRoleRules(user.role);
      });
    }
  }

  private applyRoleRules(role: string | null): void {
    const isEmp = role === 'EMPLOYEE';
    this.isEmployeeRole.set(isEmp);
    const managerControl = this.form.get('managerId')!;
    const hrControl = this.form.get('hrId')!;
    if (isEmp) {
      managerControl.setValidators([Validators.required]);
      hrControl.setValidators([Validators.required]);
    } else {
      managerControl.clearValidators();
      hrControl.clearValidators();
      managerControl.setValue(null);
      hrControl.setValue(null);
    }
    managerControl.updateValueAndValidity();
    hrControl.updateValueAndValidity();
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
      const body: AdminUpdateUserRequest = {
        name: formValue.name!,
        email: formValue.email!,
        role: formValue.role as AdminUpdateUserRequest['role'],
        status: formValue.status as AdminUpdateUserRequest['status'],
        departmentName: formValue.departmentName as AdminUpdateUserRequest['departmentName'],
        gradeId: formValue.gradeId,
        managerId: formValue.managerId,
        hrId: formValue.hrId,
        salary: formValue.salary!,
        yearsOfExperience: formValue.yearsOfExperience!,
      };
      this.users.adminUpdate(Number(this.id()), body).subscribe({
        next: () => { this.toast.success('User updated.'); this.router.navigate(['/users']); },
        error: () => this.saving.set(false),
      });
    } else {
      const body: AdminCreateUserRequest = {
        employeeId: formValue.employeeId!,
        name: formValue.name!,
        email: formValue.email!,
        password: formValue.password!,
        role: formValue.role as AdminCreateUserRequest['role'],
        departmentName: formValue.departmentName as AdminCreateUserRequest['departmentName'],
        gradeId: formValue.gradeId,
        status: formValue.status as AdminCreateUserRequest['status'],
        managerId: formValue.managerId,
        hrId: formValue.hrId,
        salary: formValue.salary!,
        yearsOfExperience: formValue.yearsOfExperience!,
      };
      this.users.create(body).subscribe({
        next: () => { this.toast.success('User created.'); this.router.navigate(['/users']); },
        error: () => this.saving.set(false),
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/users']);
  }
}
