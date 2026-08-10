import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { GoalDimension, GoalStatus } from '../models/enums';
import {
  CreateGoalRequest,
  CreateProgressRequest,
  GoalResponse,
  ProgressResponse,
  UpdateGoalRequest,
  UpdateProgressRequest,
} from '../models/goal.model';

@Injectable({ providedIn: 'root' })
export class GoalService {
  private readonly http = inject(HttpClient);

  list(
    page: PageQuery,
    filters: { dimension?: GoalDimension | null; status?: GoalStatus | null; ownerId?: number | null } = {},
  ): Observable<Page<GoalResponse>> {
    return this.http.get<Page<GoalResponse>>(apiUrl(API.goals.base), {
      params: pageParams(page, {
        dimension: filters.dimension ?? undefined,
        status: filters.status ?? undefined,
        ownerId: filters.ownerId ?? undefined,
      }),
    });
  }

  getById(id: number): Observable<GoalResponse> {
    return this.http.get<GoalResponse>(apiUrl(API.goals.byId(id)));
  }

  create(body: CreateGoalRequest): Observable<GoalResponse> {
    return this.http.post<GoalResponse>(apiUrl(API.goals.base), body);
  }

  update(id: number, body: UpdateGoalRequest): Observable<GoalResponse> {
    return this.http.put<GoalResponse>(apiUrl(API.goals.byId(id)), body);
  }

  listProgress(goalId: number, page: PageQuery): Observable<Page<ProgressResponse>> {
    return this.http.get<Page<ProgressResponse>>(apiUrl(API.goals.progress(goalId)), {
      params: pageParams(page),
    });
  }

  addProgress(goalId: number, body: CreateProgressRequest): Observable<ProgressResponse> {
    return this.http.post<ProgressResponse>(apiUrl(API.goals.progress(goalId)), body);
  }

  updateProgress(goalId: number, progressId: number, body: UpdateProgressRequest): Observable<ProgressResponse> {
    return this.http.put<ProgressResponse>(apiUrl(API.goals.progressById(goalId, progressId)), body);
  }

  confirmProgress(goalId: number, progressId: number): Observable<ProgressResponse> {
    return this.http.put<ProgressResponse>(apiUrl(API.goals.confirmProgress(goalId, progressId)), {});
  }
}
