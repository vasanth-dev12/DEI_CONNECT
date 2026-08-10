import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { AuditLogResponse } from '../models/iam.model';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);

  list(
    page: PageQuery,
    filters: { userId?: number | null; entityType?: string | null } = {},
  ): Observable<Page<AuditLogResponse>> {
    return this.http.get<Page<AuditLogResponse>>(apiUrl(API.auditLogs.base), {
      params: pageParams(page, {
        userId: filters.userId ?? undefined,
        entityType: filters.entityType ?? undefined,
      }),
    });
  }
}
