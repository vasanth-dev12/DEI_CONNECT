import { GoalDimension, GoalStatus, ProgressStatus, ProgressTrend } from './enums';

export interface CreateGoalRequest {
  goalName: string;
  dimension: GoalDimension;
  targetGroup?: string | null;
  baselineValue: number;
  targetValue: number;
  targetYear: number;
}

export interface UpdateGoalRequest {
  goalName: string;
  dimension: GoalDimension;
  targetGroup?: string | null;
  baselineValue: number;
  targetValue: number;
  targetYear: number;
  status: GoalStatus;
}

export interface GoalResponse {
  goalId: number;
  goalName: string;
  dimension: GoalDimension;
  targetGroup: string | null;
  baselineValue: number;
  targetValue: number;
  targetYear: number;
  ownerId: number;
  ownerName: string | null;
  status: GoalStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface CreateProgressRequest {
  period: string;
  actualValue: number;
  commentary?: string | null;
}

export interface UpdateProgressRequest {
  period: string;
  actualValue: number;
  commentary?: string | null;
}

export interface ProgressResponse {
  progressId: number;
  goalId: number;
  period: string;
  actualValue: number;
  gapToTarget: number;
  trend: ProgressTrend;
  commentary: string | null;
  status: ProgressStatus;
  createdDate: string;
  lastModifiedDate: string;
}
