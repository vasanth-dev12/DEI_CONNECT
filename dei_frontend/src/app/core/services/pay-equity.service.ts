import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { AnalysisStatus, PayDimension } from '../models/enums';
import {
  PayEquityAnalysisRequest,
  PayEquityAnalysisResponse,
  PayGapFlagResponse,
  PublishedPayEquityAnalysisResponse,
  PublishedPayGapFlagResponse,
  UpdatePayGapFlagRequest,
} from '../models/pay-equity.model';

@Injectable({ providedIn: 'root' })
export class PayEquityService {
  private readonly http = inject(HttpClient);

  listAnalyses(
    page: PageQuery,
    filters: { dimension?: PayDimension | null; status?: AnalysisStatus | null; hrId?: number | null } = {},
  ): Observable<Page<PayEquityAnalysisResponse>> {
    return this.http.get<Page<PayEquityAnalysisResponse>>(apiUrl(API.payEquity.analyses), {
      params: pageParams(page, {
        dimension: filters.dimension ?? undefined,
        status: filters.status ?? undefined,
        hrId: filters.hrId ?? undefined,
      }),
    });
  }

  getAnalysis(id: number): Observable<PayEquityAnalysisResponse> {
    return this.http.get<PayEquityAnalysisResponse>(apiUrl(API.payEquity.analysisById(id)));
  }

  createAnalysis(body: PayEquityAnalysisRequest): Observable<PayEquityAnalysisResponse> {
    return this.http.post<PayEquityAnalysisResponse>(apiUrl(API.payEquity.analyses), body);
  }

  updateAnalysis(id: number, body: PayEquityAnalysisRequest): Observable<PayEquityAnalysisResponse> {
    return this.http.put<PayEquityAnalysisResponse>(apiUrl(API.payEquity.analysisById(id)), body);
  }

  publishAnalysis(id: number): Observable<PayEquityAnalysisResponse> {
    return this.http.put<PayEquityAnalysisResponse>(apiUrl(API.payEquity.publishAnalysis(id)), {});
  }

  computeAnalysis(id: number): Observable<PayEquityAnalysisResponse> {
    return this.http.post<PayEquityAnalysisResponse>(apiUrl(API.payEquity.computeAnalysis(id)), {});
  }

  listFlags(analysisId: number): Observable<PayGapFlagResponse[]> {
    return this.http.get<PayGapFlagResponse[]>(apiUrl(API.payEquity.flags(analysisId)));
  }

  updateFlag(analysisId: number, flagId: number, body: UpdatePayGapFlagRequest): Observable<PayGapFlagResponse> {
    return this.http.put<PayGapFlagResponse>(apiUrl(API.payEquity.flagById(analysisId, flagId)), body);
  }

  listPublished(page: PageQuery): Observable<Page<PublishedPayEquityAnalysisResponse>> {
    return this.http.get<Page<PublishedPayEquityAnalysisResponse>>(apiUrl(API.payEquity.publishedAnalyses), {
      params: pageParams(page),
    });
  }

  getPublished(id: number): Observable<PublishedPayEquityAnalysisResponse> {
    return this.http.get<PublishedPayEquityAnalysisResponse>(apiUrl(API.payEquity.publishedAnalysisById(id)));
  }

  listPublishedFlags(analysisId: number): Observable<PublishedPayGapFlagResponse[]> {
    return this.http.get<PublishedPayGapFlagResponse[]>(apiUrl(API.payEquity.publishedFlags(analysisId)));
  }
}
