import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { DiversityService } from '../../../core/services/diversity.service';
import { ToastService } from '../../../core/services/toast.service';
import { DemographicProfileRequest, DemographicProfileResponse } from '../../../core/models/diversity.model';
import {
  ALL_AGE_GROUPS,
  ALL_CONSENT_STATUSES,
  ALL_DISABILITY_STATUSES,
  ALL_ETHNICITIES,
  ALL_GENDERS,
  ALL_VETERAN_STATUSES,
} from '../../../core/models/enums';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-my-demographic-profile',
  standalone: true,
  imports: [ReactiveFormsModule, PageHeaderComponent, EnumLabelPipe],
  templateUrl: './my-demographic-profile.component.html',
})
export class MyDemographicProfileComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly diversityService = inject(DiversityService);
  private readonly notificationService = inject(ToastService);

  readonly availableConsentStatuses = ALL_CONSENT_STATUSES;
  readonly availableDisabilityStatuses = ALL_DISABILITY_STATUSES;
  readonly availableGenders = ALL_GENDERS;
  readonly availableEthnicities = ALL_ETHNICITIES;
  readonly availableVeteranStatuses = ALL_VETERAN_STATUSES;
  readonly availableAgeGroups = ALL_AGE_GROUPS;

  readonly isProfileExisting = signal(false);
  readonly isSaving = signal(false);

  readonly profileForm = this.formBuilder.group({
    gender: ['PREFER_NOT_TO_SAY', [Validators.required]],
    ethnicity: ['PREFER_NOT_TO_SAY', [Validators.required]],
    disability: ['PREFER_NOT_TO_SAY', [Validators.required]],
    veteranStatus: ['PREFER_NOT_TO_SAY', [Validators.required]],
    ageGroup: ['PREFER_NOT_TO_SAY', [Validators.required]],
    consentStatus: ['CONSENTED', [Validators.required]],
  });

  ngOnInit(): void {
    this.diversityService.getOwnProfile().subscribe({
      next: (profile) => {
        this.isProfileExisting.set(true);
        this.patchProfileForm(profile);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 404) {
          this.isProfileExisting.set(false);
        }
      },
    });
  }

  private patchProfileForm(profile: DemographicProfileResponse): void {
    this.profileForm.patchValue({
      gender: profile.gender ?? 'PREFER_NOT_TO_SAY',
      ethnicity: profile.ethnicity ?? 'PREFER_NOT_TO_SAY',
      disability: profile.disability ?? 'PREFER_NOT_TO_SAY',
      veteranStatus: profile.veteranStatus ?? 'PREFER_NOT_TO_SAY',
      ageGroup: profile.ageGroup ?? 'PREFER_NOT_TO_SAY',
      consentStatus: profile.consentStatus,
    });
  }

  isFormControlInvalid(controlName: string): boolean {
    const control = this.profileForm.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.notificationService.error('Please correct the highlighted fields.');
      return;
    }
    this.isSaving.set(true);
    const formValue = this.profileForm.getRawValue();
    const payload: DemographicProfileRequest = {
      gender: formValue.gender as DemographicProfileRequest['gender'],
      ethnicity: formValue.ethnicity as DemographicProfileRequest['ethnicity'],
      disability: formValue.disability as DemographicProfileRequest['disability'],
      veteranStatus: formValue.veteranStatus as DemographicProfileRequest['veteranStatus'],
      ageGroup: formValue.ageGroup as DemographicProfileRequest['ageGroup'],
      consentStatus: formValue.consentStatus as DemographicProfileRequest['consentStatus'],
    };

    const saveRequest$ = this.isProfileExisting()
      ? this.diversityService.updateOwnProfile(payload)
      : this.diversityService.createProfile(payload);

    saveRequest$.subscribe({
      next: (profile) => {
        this.isProfileExisting.set(true);
        this.patchProfileForm(profile);
        this.isSaving.set(false);
        this.notificationService.success('Demographic profile saved.');
      },
      error: () => this.isSaving.set(false),
    });
  }
}