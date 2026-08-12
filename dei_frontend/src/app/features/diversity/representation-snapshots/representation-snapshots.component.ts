import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { DiversityService } from '../../../core/services/diversity.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  GenerateSnapshotRequest,
  RepresentationSnapshotResponse,
  SnapshotRunResponse,
} from '../../../core/models/diversity.model';
import {
  ALL_DEMOGRAPHIC_DIMENSIONS,
  ALL_DEPARTMENTS,
  ALL_SNAPSHOT_STATUSES,
  DemographicDimension,
  DepartmentName,
  SnapshotStatus,
} from '../../../core/models/enums';
import { Page } from '../../../core/models/common.model';

import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { PaginatorComponent } from '../../../shared/components/paginator/paginator.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { HasRoleDirective } from '../../../shared/directives/has-role.directive';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';

@Component({
  selector: 'app-representation-snapshots',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, FormsModule, ReactiveFormsModule,
    PageHeaderComponent, PaginatorComponent, EmptyStateComponent, StatusBadgeComponent,
    StatCardComponent, ConfirmModalComponent, HasRoleDirective, EnumLabelPipe,
  ],
  templateUrl: './representation-snapshots.component.html',
})
export class RepresentationSnapshotsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly diversityService = inject(DiversityService);
  private readonly toastService = inject(ToastService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly defaultSnapshotDate = this.getTodayDateString();

  readonly generateForm = this.fb.group({
    snapshotDate: [this.defaultSnapshotDate, [Validators.required]],
    dimension: ['GENDER' as DemographicDimension, [Validators.required]],
    departmentName: [null as DepartmentName | null],
  });

  readonly availableDimensions = ALL_DEMOGRAPHIC_DIMENSIONS;
  readonly availableDepartments = ALL_DEPARTMENTS;
  readonly availableStatuses = ALL_SNAPSHOT_STATUSES;

  readonly isPublishedOnlyView = this.authService.role() === 'EXECUTIVE' || this.authService.role() === 'ADMIN';
  readonly canGenerateSnapshots = this.authService.role() === 'DEI_MANAGER';

  dimensionFilter: DemographicDimension | null = null;
  departmentFilter: DepartmentName | null = null;
  statusFilter: SnapshotStatus | null = null;
  private currentPageIndex = 0;

  readonly snapshotPage = signal<Page<SnapshotRunResponse> | null>(null);
  readonly isGenerateModalOpen = signal(false);
  readonly isGenerating = signal(false);
  readonly selectedSnapshot = signal<SnapshotRunResponse | null>(null);
  readonly snapshotPendingDeletion = signal<SnapshotRunResponse | null>(null);

  readonly visibleChartGroups = computed(() => {
    const snapshot = this.selectedSnapshot();
    if (!snapshot) return [];
    return snapshot.groups
      .filter((group) => !group.suppressed && group.percentage !== null)
      .sort((a, b) => (b.percentage ?? 0) - (a.percentage ?? 0));
  });

  readonly suppressedGroups = computed<RepresentationSnapshotResponse[]>(
    () => this.selectedSnapshot()?.groups.filter((group) => group.suppressed) ?? [],
  );

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.has('generate') && this.canGenerateSnapshots) {
      this.isGenerateModalOpen.set(true);
    }
    this.reloadSnapshots(0);
  } 

  reloadSnapshots(pageIndex: number): void {
    this.currentPageIndex = pageIndex;
    const pageParams = { page: pageIndex, size: 10, sort: 'snapshotDate,desc' };
    const filterParams = {
      dimension: this.dimensionFilter,
      departmentName: this.departmentFilter,
      status: this.isPublishedOnlyView ? 'PUBLISHED' : this.statusFilter,
    };

    this.diversityService
      .listSnapshotRuns(pageParams, filterParams)
      .subscribe((pagedResult) => {
        this.snapshotPage.set(pagedResult);
      });
  }

  generateSnapshot(): void {
    if (this.generateForm.invalid) {
      this.generateForm.markAllAsTouched();
      this.toastService.error('Please correct the highlighted fields.');
      return;
    }

    this.isGenerating.set(true);
    const formValue = this.generateForm.getRawValue();

    const payload: GenerateSnapshotRequest = {
      snapshotDate: this.defaultSnapshotDate,
      dimension: formValue.dimension as DemographicDimension,
      ...(formValue.departmentName ? { departmentName: formValue.departmentName } : {}),
    };

    this.diversityService.generate(payload).subscribe({
      next: (result) => {
        this.isGenerating.set(false);
        this.isGenerateModalOpen.set(false);

        if (result.snapshots.length === 0) {
          this.toastService.info(
            `No ${result.dimension} groups could be shown: all ${result.suppressedGroupCount} group(s) ` +
              `fall below the minimum group size required to protect privacy.`,
          );
        } else {
          this.toastService.success(
            `Generated ${result.snapshots.length} ${result.dimension} group(s)` +
              (result.suppressedGroupCount > 0 ? `, ${result.suppressedGroupCount} suppressed for privacy.` : '.'),
          );
        }
        this.reloadSnapshots(0);
      },
      error: () => this.isGenerating.set(false),
    });
  }

  viewSnapshot(snapshot: SnapshotRunResponse): void {
    this.selectedSnapshot.set(snapshot);
  }

  closeViewModal(): void {
    this.selectedSnapshot.set(null);
  }

  publishSnapshot(snapshot: SnapshotRunResponse): void {
    this.diversityService.publishSnapshotRun(snapshot.snapshotId).subscribe(() => {
      this.toastService.success(`${snapshot.dimension} snapshot published.`);
      this.closeViewModal();
      this.reloadSnapshots(this.currentPageIndex);
    });
  }

  openDeleteConfirmation(snapshot: SnapshotRunResponse): void {
    this.snapshotPendingDeletion.set(snapshot);
  }

  confirmDelete(): void {
    const snapshot = this.snapshotPendingDeletion();
    if (!snapshot) return;

    this.diversityService.deleteSnapshotRun(snapshot.snapshotId).subscribe(() => {
      this.toastService.success('Snapshot deleted.');
      this.snapshotPendingDeletion.set(null);
      this.closeViewModal();
      this.reloadSnapshots(this.currentPageIndex);
    });
  }

  isFormControlInvalid(controlName: string): boolean {
    const control = this.generateForm.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  private getTodayDateString(): string {
    return new Date().toISOString().slice(0, 10);
  }
}