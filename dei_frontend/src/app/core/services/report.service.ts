import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { ReportStatus } from '../models/enums';
import {
  DEIReportDataResponse,
  DEIReportRequest,
  DEIReportResponse,
} from '../models/reporting.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);

  list(page: PageQuery, status?: ReportStatus | null): Observable<Page<DEIReportResponse>> {
    return this.http.get<Page<DEIReportResponse>>(apiUrl(API.reports.base), {
      params: pageParams(page, { status: status ?? undefined }),
    });
  }

  getById(id: number): Observable<DEIReportResponse> {
    return this.http.get<DEIReportResponse>(apiUrl(API.reports.byId(id)));
  }

  getData(id: number): Observable<DEIReportDataResponse> {
    return this.http.get<DEIReportDataResponse>(apiUrl(API.reports.data(id)));
  }

  create(body: DEIReportRequest): Observable<DEIReportResponse> {
    return this.http.post<DEIReportResponse>(apiUrl(API.reports.base), body);
  }

  update(id: number, body: DEIReportRequest): Observable<DEIReportResponse> {
    return this.http.put<DEIReportResponse>(apiUrl(API.reports.byId(id)), body);
  }

  publish(id: number): Observable<DEIReportResponse> {
    return this.http.put<DEIReportResponse>(apiUrl(API.reports.publish(id)), {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.reports.byId(id)));
  }
}
