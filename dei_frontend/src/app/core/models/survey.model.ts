import {
  QuestionType,
  SummaryScope,
  SummaryStatus,
  SurveyDimension,
  SurveyStatus,
  SurveyType,
} from './enums';

export interface CreateQuestionRequest {
  questionText: string;
  questionType: QuestionType;
  dimension: SurveyDimension;
  mandatory: boolean;
  sequenceOrder?: number | null;
}

export interface UpdateQuestionRequest {
  questionText: string;
  questionType: QuestionType;
  dimension: SurveyDimension;
  mandatory: boolean;
  sequenceOrder?: number | null;
}

export interface QuestionResponse {
  questionId: number;
  surveyId: number;
  questionText: string;
  questionType: QuestionType;
  dimension: SurveyDimension;
  mandatory: boolean;
  sequenceOrder: number;
  creatorManagerId: number | null;
  creatorManagerName: string | null;
}

export interface CreateSurveyRequest {
  surveyName: string;
  surveyType: SurveyType;
  launchDate?: string | null;
  closeDate?: string | null;
  anonymised?: boolean | null;
  minResponseThreshold: number;
  questions?: CreateQuestionRequest[];
}

export interface UpdateSurveyRequest {
  surveyName: string;
  surveyType: SurveyType;
  launchDate?: string | null;
  closeDate?: string | null;
  anonymised?: boolean | null;
  minResponseThreshold: number;
}

export interface SurveyResponse {
  surveyId: number;
  surveyName: string;
  surveyType: SurveyType;
  launchDate: string | null;
  closeDate: string | null;
  anonymised: boolean | null;
  minResponseThreshold: number;
  status: SurveyStatus;
  creatorManagerId: number | null;
  respondedByMe: boolean | null;
  questions: QuestionResponse[] | null;
}

export interface AnswerItem {
  questionId: number;
  numericValue: number | null;
}

export interface SubmitSurveyRequest {
  answers: AnswerItem[];
}

export interface SubmitAcknowledgement {
  surveyId: number;
  accepted: boolean;
  message: string;
}

export interface SummaryResponse {
  summaryId: number;
  surveyId: number;
  scope: SummaryScope;
  scopeValue: string | null;
  respondentCount: number | null;
  inclusionIndex: number | null;
  keyThemeSentiment: string | null;
  status: SummaryStatus;
  suppressed: boolean;
}
