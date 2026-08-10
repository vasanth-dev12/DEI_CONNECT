import { AnalysisStatus, ControlVariable, FlagStatus, PayDimension } from './enums';

export interface PayEquityAnalysisRequest {
  analysisPeriod: string;
  dimension: PayDimension;
  controlVariables: ControlVariable[];
}

export interface PayEquityAnalysisResponse {
  id: number;
  analysisPeriod: string;
  dimension: PayDimension;
  controlVariables: ControlVariable[];
  medianGapPercent: number | null;
  adjustedGapPercent: number | null;
  significanceLevel: number | null;
  runById: number | null;
  runByName: string | null;
  status: AnalysisStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface PublishedPayEquityAnalysisResponse {
  id: number;
  analysisPeriod: string;
  dimension: PayDimension;
  controlVariables: ControlVariable[];
  medianGapPercent: number | null;
  adjustedGapPercent: number | null;
  significanceLevel: number | null;
  status: AnalysisStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface UpdatePayGapFlagRequest {
  remediationOwnerId: number;
  status: FlagStatus;
}

export interface PayGapFlagResponse {
  id: number;
  analysisId: number;
  departmentId: number | null;
  gradeId: number | null;
  groupName: string;
  gapPercent: number;
  affectedEmployeeCount: number;
  remediationOwnerId: number | null;
  remediationOwnerName: string | null;
  status: FlagStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface PublishedPayGapFlagResponse {
  id: number;
  analysisId: number;
  departmentId: number | null;
  gradeId: number | null;
  groupName: string;
  gapPercent: number | null;
  affectedEmployeeCount: number | null;
  status: FlagStatus;
  suppressed: boolean;
}
