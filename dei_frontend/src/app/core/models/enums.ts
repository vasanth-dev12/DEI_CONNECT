export type Role =
  | 'EMPLOYEE'
  | 'DEI_MANAGER'
  | 'HR_BIZ_PARTNER'
  | 'ERG_LEAD'
  | 'EXECUTIVE'
  | 'ADMIN';

export const ALL_ROLES: Role[] = [
  'EMPLOYEE',
  'DEI_MANAGER',
  'HR_BIZ_PARTNER',
  'ERG_LEAD',
  'EXECUTIVE',
  'ADMIN',
];

export type UserStatus = 'ACTIVE' | 'INACTIVE';

export type DepartmentName =
  | 'INFRASTRUCTURE'
  | 'CYBERSECURITY'
  | 'HR'
  | 'SOFTWARE_ENGINEERING'
  | 'END_USER_SUPPORT';

export const ALL_DEPARTMENTS: DepartmentName[] = [
  'INFRASTRUCTURE',
  'CYBERSECURITY',
  'HR',
  'SOFTWARE_ENGINEERING',
  'END_USER_SUPPORT',
];

export type GradeName =
  | 'ENTRY_LEVEL'
  | 'ASSOCIATE'
  | 'SENIOR_ASSOCIATE'
  | 'SPECIALIST'
  | 'MANAGER'
  | 'SENIOR_MANAGER'
  | 'DIRECTOR'
  | 'VICE_PRESIDENT';

export const ALL_GRADES: GradeName[] = [
  'ENTRY_LEVEL',
  'ASSOCIATE',
  'SENIOR_ASSOCIATE',
  'SPECIALIST',
  'MANAGER',
  'SENIOR_MANAGER',
  'DIRECTOR',
  'VICE_PRESIDENT',
];

export type ConsentStatus = 'CONSENTED' | 'DECLINED' | 'PENDING';
export const ALL_CONSENT_STATUSES: ConsentStatus[] = ['CONSENTED', 'DECLINED', 'PENDING'];

export type DisabilityStatus = 'YES' | 'NO' | 'PREFER_NOT_TO_SAY';
export const ALL_DISABILITY_STATUSES: DisabilityStatus[] = ['YES', 'NO', 'PREFER_NOT_TO_SAY'];

export type DemographicDimension = 'GENDER' | 'ETHNICITY' | 'DISABILITY' | 'VETERAN' | 'AGE_GROUP';
export const ALL_DEMOGRAPHIC_DIMENSIONS: DemographicDimension[] = ['GENDER', 'ETHNICITY', 'DISABILITY', 'VETERAN', 'AGE_GROUP'];

export type SnapshotStatus = 'DRAFT' | 'PUBLISHED';
export const ALL_SNAPSHOT_STATUSES: SnapshotStatus[] = ['DRAFT', 'PUBLISHED'];

export type Gender = 'MALE' | 'FEMALE' | 'NON_BINARY' | 'PREFER_NOT_TO_SAY' | 'OTHER';
export const ALL_GENDERS: Gender[] = ['MALE', 'FEMALE', 'NON_BINARY', 'PREFER_NOT_TO_SAY', 'OTHER'];

export type Ethnicity =
  | 'ASIAN'
  | 'BLACK_OR_AFRICAN_AMERICAN'
  | 'HISPANIC_OR_LATINO'
  | 'WHITE'
  | 'TWO_OR_MORE_RACES'
  | 'OTHER'
  | 'PREFER_NOT_TO_SAY';
export const ALL_ETHNICITIES: Ethnicity[] = [
  'ASIAN',
  'BLACK_OR_AFRICAN_AMERICAN',
  'HISPANIC_OR_LATINO',
  'WHITE',
  'TWO_OR_MORE_RACES',
  'OTHER',
  'PREFER_NOT_TO_SAY',
];

export type AgeGroup = 'UNDER_25' | 'AGE_25_34' | 'AGE_35_44' | 'AGE_45_54' | 'AGE_55_PLUS' | 'PREFER_NOT_TO_SAY';
export const ALL_AGE_GROUPS: AgeGroup[] = ['UNDER_25', 'AGE_25_34', 'AGE_35_44', 'AGE_45_54', 'AGE_55_PLUS', 'PREFER_NOT_TO_SAY'];

export type VeteranStatus = 'YES' | 'NO' | 'PREFER_NOT_TO_SAY';
export const ALL_VETERAN_STATUSES: VeteranStatus[] = ['YES', 'NO', 'PREFER_NOT_TO_SAY'];

export type GoalDimension = 'GENDER' | 'ETHNICITY' | 'DISABILITY' | 'ERG_MEMBERSHIP' | 'PAY_EQUITY';
export const ALL_GOAL_DIMENSIONS: GoalDimension[] = ['GENDER', 'ETHNICITY', 'DISABILITY', 'ERG_MEMBERSHIP', 'PAY_EQUITY'];

export type GoalStatus = 'ACTIVE' | 'ACHIEVED' | 'OFF_TRACK' | 'SUPERSEDED';
export const ALL_GOAL_STATUSES: GoalStatus[] = ['ACTIVE', 'ACHIEVED', 'OFF_TRACK', 'SUPERSEDED'];

export type ProgressStatus = 'DRAFT' | 'CONFIRMED';
export type ProgressTrend = 'IMPROVING' | 'STATIC' | 'WORSENING';

export type AnalysisStatus = 'DRAFT' | 'PUBLISHED';
export const ALL_ANALYSIS_STATUSES: AnalysisStatus[] = ['DRAFT', 'PUBLISHED'];

export type ControlVariable = 'GRADE' | 'ROLE' | 'TENURE' | 'PERFORMANCE';
export const ALL_CONTROL_VARIABLES: ControlVariable[] = ['GRADE', 'ROLE', 'TENURE', 'PERFORMANCE'];

export type FlagStatus = 'OPEN' | 'REMEDIATION_IN_PROGRESS' | 'RESOLVED';
export const ALL_FLAG_STATUSES: FlagStatus[] = ['OPEN', 'REMEDIATION_IN_PROGRESS', 'RESOLVED'];

export type PayDimension = 'GENDER' | 'ETHNICITY';
export const ALL_PAY_DIMENSIONS: PayDimension[] = ['GENDER', 'ETHNICITY'];

export type ReportMetric =
  | 'REPRESENTATION_BY_DIMENSION'
  | 'INCLUSION_INDEX'
  | 'ERG_MEMBERSHIP_RATE'
  | 'GOAL_ATTAINMENT_RATE'
  | 'PAY_EQUITY_GAP';
export const ALL_REPORT_METRICS: ReportMetric[] = [
  'REPRESENTATION_BY_DIMENSION',
  'INCLUSION_INDEX',
  'ERG_MEMBERSHIP_RATE',
  'GOAL_ATTAINMENT_RATE',
  'PAY_EQUITY_GAP',
];

export type ReportScope = 'ORGANISATION' | 'DEPARTMENT' | 'GRADE' | 'PERIOD';
export const ALL_REPORT_SCOPES: ReportScope[] = ['ORGANISATION', 'DEPARTMENT', 'GRADE', 'PERIOD'];

export type ReportStatus = 'DRAFT' | 'PUBLISHED';
export const ALL_REPORT_STATUSES: ReportStatus[] = ['DRAFT', 'PUBLISHED'];

export type NotificationCategory = 'SURVEY' | 'GOAL' | 'ERG' | 'PAY_EQUITY' | 'REPORT' | 'PROGRAM';
export const ALL_NOTIFICATION_CATEGORIES: NotificationCategory[] = ['SURVEY', 'GOAL', 'ERG', 'PAY_EQUITY', 'REPORT', 'PROGRAM'];

export type NotificationStatus = 'UNREAD' | 'READ' | 'DISMISSED';
export const ALL_NOTIFICATION_STATUSES: NotificationStatus[] = ['UNREAD', 'READ', 'DISMISSED'];

export type SurveyType = 'ANNUAL' | 'PULSE_CHECK' | 'ONBOARDING' | 'EXIT_SENTIMENT';
export const ALL_SURVEY_TYPES: SurveyType[] = ['ANNUAL', 'PULSE_CHECK', 'ONBOARDING', 'EXIT_SENTIMENT'];

export type SurveyStatus = 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'PUBLISHED';
export const ALL_SURVEY_STATUSES: SurveyStatus[] = ['DRAFT', 'ACTIVE', 'CLOSED', 'PUBLISHED'];

export type QuestionType = 'LIKERT_SCALE' | 'BINARY';
export const ALL_QUESTION_TYPES: QuestionType[] = ['LIKERT_SCALE', 'BINARY'];

export type SurveyDimension = 'BELONGING' | 'FAIRNESS' | 'RECOGNITION' | 'VOICE_SAFETY' | 'ADVANCEMENT';
export const ALL_SURVEY_DIMENSIONS: SurveyDimension[] = ['BELONGING', 'FAIRNESS', 'RECOGNITION', 'VOICE_SAFETY', 'ADVANCEMENT'];

export type SummaryScope = 'DEPARTMENT' | 'GRADE' | 'DEMOGRAPHIC' | 'MANAGER' | 'HR';
export type SummaryStatus = 'COMPUTED' | 'PUBLISHED';

export type ErgFocus = 'GENDER' | 'ETHNICITY' | 'LGBTQ' | 'DISABILITY' | 'VETERANS' | 'WELLBEING' | 'GENERATIONS';
export const ALL_ERG_FOCUSES: ErgFocus[] = ['GENDER', 'ETHNICITY', 'LGBTQ', 'DISABILITY', 'VETERANS', 'WELLBEING', 'GENERATIONS'];

export type ErgStatus = 'ACTIVE' | 'INACTIVE';
export const ALL_ERG_STATUSES: ErgStatus[] = ['ACTIVE', 'INACTIVE'];

export type EventType = 'NETWORKING_SESSION' | 'WORKSHOP' | 'SPEAKER_PANEL' | 'COMMUNITY_ACTION' | 'CELEBRATION';
export const ALL_EVENT_TYPES: EventType[] = ['NETWORKING_SESSION', 'WORKSHOP', 'SPEAKER_PANEL', 'COMMUNITY_ACTION', 'CELEBRATION'];

export type EventStatus = 'PLANNED' | 'COMPLETED' | 'CANCELLED';
export const ALL_EVENT_STATUSES: EventStatus[] = ['PLANNED', 'COMPLETED', 'CANCELLED'];

export type MembershipRole = 'MEMBER' | 'CHAPTER_LEAD' | 'COMMITTEE_MEMBER';
export const ALL_MEMBERSHIP_ROLES: MembershipRole[] = ['MEMBER', 'CHAPTER_LEAD', 'COMMITTEE_MEMBER'];

export type MembershipStatus = 'ACTIVE' | 'INACTIVE';
export const ALL_MEMBERSHIP_STATUSES: MembershipStatus[] = ['ACTIVE', 'INACTIVE'];
