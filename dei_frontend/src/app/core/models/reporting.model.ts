import { ReportMetric, ReportScope, ReportStatus } from './enums';
import { RepresentationSnapshotResponse } from './diversity.model';

export interface DEIReportRequest {
  scope: ReportScope;
  scopeValue?: string | null;
  metrics: ReportMetric[];
}

export interface DEIReportResponse {
  id: number;
  scope: ReportScope;
  scopeValue: string | null;
  metrics: ReportMetric[];
  generatedDate: string;
  status: ReportStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface DEIReportDataResponse {
  reportId: number;
  scope: ReportScope;
  scopeValue: string | null;
  generatedDate: string;
  representation: RepresentationSnapshotResponse[] | null;
  inclusionIndex: number | null;
  ergMembershipRate: number | null;
  goalAttainmentRate: number | null;
  payEquityGap: number | null;
}
