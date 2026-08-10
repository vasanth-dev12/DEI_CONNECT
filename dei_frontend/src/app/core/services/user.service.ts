import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams, toParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { Role } from '../models/enums';
import {
  AdminCreateUserRequest,
  AdminUpdateUserRequest,
  ScopeValueOption,
  UpdateProfileRequest,
  UserResponse,
} from '../models/iam.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  list(page: PageQuery, role?: Role | null): Observable<Page<UserResponse>> {
    return this.http.get<Page<UserResponse>>(apiUrl(API.users.base), {
      params: pageParams(page, { role: role ?? undefined }),
    });
  }

  listByRole(role: Role, size = 100): Observable<Page<UserResponse>> {
    return this.http.get<Page<UserResponse>>(apiUrl(API.users.base), {
      params: toParams({ role, page: 0, size }),
    });
  }

  me(): Observable<UserResponse> {
    return this.http.get<UserResponse>(apiUrl(API.users.me));
  }

  scopeValues(scope: 'DEPARTMENT' | 'GRADE'): Observable<ScopeValueOption[]> {
    return this.http.get<ScopeValueOption[]>(apiUrl(API.users.scopeValues), {
      params: toParams({ scope }),
    });
  }

  getById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(apiUrl(API.users.byId(id)));
  }

  create(body: AdminCreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(apiUrl(API.users.base), body);
  }

  updateProfile(body: UpdateProfileRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(apiUrl(API.users.me), body);
  }

  adminUpdate(id: number, body: AdminUpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(apiUrl(API.users.byId(id)), body);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.users.byId(id)));
  }
}
