import { AgeGroup, ConsentStatus, DemographicDimension, DepartmentName, DisabilityStatus, Ethnicity, Gender, SnapshotStatus, VeteranStatus } from './enums';

export interface DemographicProfileRequest {
  gender?: Gender | null;
  ethnicity?: Ethnicity | null;
  disability?: DisabilityStatus | null;
  veteranStatus?: VeteranStatus | null;
  ageGroup?: AgeGroup | null;
  dataCollectedDate?: string | null;
  consentStatus: ConsentStatus;
}

export interface DemographicProfileResponse {
  profileId: number;
  employeeId: string;
  gender: Gender | null;
  ethnicity: Ethnicity | null;
  disability: DisabilityStatus | null;
  veteranStatus: VeteranStatus | null;
  ageGroup: AgeGroup | null;
  dataCollectedDate: string | null;
  consentStatus: ConsentStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface GenerateSnapshotRequest {
  snapshotDate: string;
  dimension: DemographicDimension;
  departmentName?: DepartmentName | null;
}

export interface RepresentationSnapshotResponse {
  snapshotId: number;
  snapshotDate: string;
  departmentId: number | null;
  departmentName: DepartmentName | null;
  dimension: DemographicDimension;
  groupName: string;
  count: number | null;
  percentage: number | null;
  status: SnapshotStatus;
  suppressed: boolean;
}

export interface SnapshotGroupResponse {
  snapshotDate: string;
  dimension: DemographicDimension;
  departmentId: number | null;
  departmentName: DepartmentName | null;
  totalHeadCount: number;
  groupCount: number;
  suppressedGroupCount: number;
  totalConsidered: number;
  groups: RepresentationSnapshotResponse[];
}

export interface SnapshotRunResponse {
  snapshotId: number;
  snapshotDate: string;
  dimension: DemographicDimension;
  departmentId: number | null;
  departmentName: DepartmentName | null;
  status: SnapshotStatus;
  groupCount: number;
  suppressedGroupCount: number;
  totalHeadCount: number;
  totalConsidered: number;
  groups: RepresentationSnapshotResponse[];
}

export interface GenerateSnapshotResult {
  dimension: DemographicDimension;
  totalConsentedConsidered: number;
  snapshots: RepresentationSnapshotResponse[];
  suppressedGroupCount: number;
}
