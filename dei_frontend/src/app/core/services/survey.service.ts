import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { SurveyStatus } from '../models/enums';
import {
  CreateQuestionRequest,
  CreateSurveyRequest,
  QuestionResponse,
  SubmitAcknowledgement,
  SubmitSurveyRequest,
  SummaryResponse,
  SurveyResponse,
  UpdateQuestionRequest,
  UpdateSurveyRequest,
} from '../models/survey.model';

@Injectable({ providedIn: 'root' })
export class SurveyService {
  private readonly http = inject(HttpClient);

  list(page: PageQuery, status?: SurveyStatus | null): Observable<Page<SurveyResponse>> {
    return this.http.get<Page<SurveyResponse>>(apiUrl(API.surveys.base), {
      params: pageParams(page, { status: status ?? undefined }),
    });
  }

  getById(id: number): Observable<SurveyResponse> {
    return this.http.get<SurveyResponse>(apiUrl(API.surveys.byId(id)));
  }

  create(body: CreateSurveyRequest): Observable<SurveyResponse> {
    return this.http.post<SurveyResponse>(apiUrl(API.surveys.base), body);
  }

  update(id: number, body: UpdateSurveyRequest): Observable<SurveyResponse> {
    return this.http.put<SurveyResponse>(apiUrl(API.surveys.byId(id)), body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.surveys.byId(id)));
  }

  launch(id: number): Observable<SurveyResponse> {
    return this.http.put<SurveyResponse>(apiUrl(API.surveys.launch(id)), {});
  }

  close(id: number): Observable<SurveyResponse> {
    return this.http.put<SurveyResponse>(apiUrl(API.surveys.close(id)), {});
  }

  publish(id: number): Observable<SurveyResponse> {
    return this.http.put<SurveyResponse>(apiUrl(API.surveys.publish(id)), {});
  }

  listQuestions(surveyId: number): Observable<QuestionResponse[]> {
    return this.http.get<QuestionResponse[]>(apiUrl(API.surveys.questions(surveyId)));
  }

  addQuestion(surveyId: number, body: CreateQuestionRequest): Observable<QuestionResponse> {
    return this.http.post<QuestionResponse>(apiUrl(API.surveys.questions(surveyId)), body);
  }

  updateQuestion(surveyId: number, questionId: number, body: UpdateQuestionRequest): Observable<QuestionResponse> {
    return this.http.put<QuestionResponse>(apiUrl(API.surveys.questionById(surveyId, questionId)), body);
  }

  deleteQuestion(surveyId: number, questionId: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.surveys.questionById(surveyId, questionId)));
  }

  submit(surveyId: number, body: SubmitSurveyRequest): Observable<SubmitAcknowledgement> {
    return this.http.post<SubmitAcknowledgement>(apiUrl(API.surveys.responses(surveyId)), body);
  }

  listSummaries(surveyId: number, page: PageQuery): Observable<Page<SummaryResponse>> {
    return this.http.get<Page<SummaryResponse>>(apiUrl(API.surveys.summaries(surveyId)), {
      params: pageParams(page),
    });
  }

  publishSummary(surveyId: number, summaryId: number): Observable<SummaryResponse> {
    return this.http.put<SummaryResponse>(apiUrl(API.surveys.publishSummary(surveyId, summaryId)), {});
  }
}
