import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { UpdateProfileRequest, UserResponse } from '../../core/models/iam.model';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { RoleLabelPipe } from '../../shared/pipes/role-label.pipe';
import { EnumLabelPipe } from '../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, RoleLabelPipe, EnumLabelPipe],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly users = inject(UserService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly me = signal<UserResponse | null>(null);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8)]],
  });

  ngOnInit(): void {
    this.users.me().subscribe((user) => {
      this.me.set(user);
      this.form.patchValue({ name: user.name, email: user.email });
    });
  }

  invalid(ctrl: string): boolean {
    const control = this.form.get(ctrl);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const formValue = this.form.getRawValue();
    const body: UpdateProfileRequest = {
      name: formValue.name,
      email: formValue.email,
      ...(formValue.password ? { password: formValue.password } : {}),
    };
    this.users.updateProfile(body).subscribe({
      next: (user) => {
        this.me.set(user);
        this.auth.syncCurrentUser({ name: user.name, email: user.email });
        this.form.patchValue({ password: '' });
        this.saving.set(false);
        this.toast.success('Profile updated.');
      },
      error: () => this.saving.set(false),
    });
  }
}
