import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { DemographicDimension, DepartmentName, SnapshotStatus } from '../models/enums';
import {
  DemographicProfileRequest,
  DemographicProfileResponse,
  GenerateSnapshotRequest,
  GenerateSnapshotResult,
  RepresentationSnapshotResponse,
  SnapshotGroupResponse,
  SnapshotRunResponse,
} from '../models/diversity.model';

@Injectable({ providedIn: 'root' })
export class DiversityService {
  private readonly http = inject(HttpClient);

  createProfile(body: DemographicProfileRequest): Observable<DemographicProfileResponse> {
    return this.http.post<DemographicProfileResponse>(apiUrl(API.demographicProfiles.base), body);
  }

  getOwnProfile(): Observable<DemographicProfileResponse> {
    return this.http.get<DemographicProfileResponse>(apiUrl(API.demographicProfiles.me));
  }

  updateOwnProfile(body: DemographicProfileRequest): Observable<DemographicProfileResponse> {
    return this.http.put<DemographicProfileResponse>(apiUrl(API.demographicProfiles.me), body);
  } 

  generate(body: GenerateSnapshotRequest): Observable<GenerateSnapshotResult> {
    return this.http.post<GenerateSnapshotResult>(apiUrl(API.snapshots.generate), body);
  }

  listSnapshotRuns(
    page: PageQuery,
    filters: {
      dimension?: DemographicDimension | null;
      departmentName?: DepartmentName | null;
      status?: SnapshotStatus | null;
    } = {},
  ): Observable<Page<SnapshotRunResponse>> {
    return this.http.get<Page<SnapshotRunResponse>>(apiUrl(API.snapshots.runs), {
      params: pageParams(page, {
        dimensionFilter: filters.dimension ?? undefined,
        departmentFilter: filters.departmentName ?? undefined,
        statusFilter: filters.status ?? undefined,
      }),
    });
  }

  listSnapshots(
    page: PageQuery,
    filters: {
      dimension?: DemographicDimension | null;
      departmentName?: DepartmentName | null;
      status?: SnapshotStatus | null;
    } = {},
  ): Observable<Page<RepresentationSnapshotResponse>> {
    return this.http.get<Page<RepresentationSnapshotResponse>>(apiUrl(API.snapshots.base), {
      params: pageParams(page, {
        dimensionFilter: filters.dimension ?? undefined,
        departmentFilter: filters.departmentName ?? undefined,
        statusFilter: filters.status ?? undefined,
      }),
    });
  }

  getSnapshot(id: number): Observable<RepresentationSnapshotResponse> {
    return this.http.get<RepresentationSnapshotResponse>(apiUrl(API.snapshots.byId(id)));
  }

  getSnapshotDistribution(id: number): Observable<SnapshotGroupResponse> {
    return this.http.get<SnapshotGroupResponse>(apiUrl(API.snapshots.distribution(id)));
  }

  publishSnapshotRun(id: number): Observable<SnapshotRunResponse> {
    return this.http.put<SnapshotRunResponse>(apiUrl(API.snapshots.publishRun(id)), {});
  }

  deleteSnapshotRun(id: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.snapshots.deleteRun(id)));
  }

  publishSnapshot(id: number): Observable<RepresentationSnapshotResponse> {
    return this.http.put<RepresentationSnapshotResponse>(apiUrl(API.snapshots.publish(id)), {});
  }

  deleteSnapshot(id: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.snapshots.byId(id)));
  }
}
