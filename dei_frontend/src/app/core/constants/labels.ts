import { Role } from '../models/enums';

export const ROLE_LABELS: Record<Role, string> = {
  EMPLOYEE: 'Employee',
  DEI_MANAGER: 'Manager',
  HR_BIZ_PARTNER: 'HR Partner',
  ERG_LEAD: 'ERG Lead',
  EXECUTIVE: 'Executive',
  ADMIN: 'Administrator',
};

export function roleLabel(role: Role | string | null | undefined): string {
  if (!role) return '';
  return ROLE_LABELS[role as Role] ?? humanize(role);
}

const ENUM_LABEL_OVERRIDES: Record<string, string> = {
  LGBTQ_PLUS: 'LGBTQ+',
  PREFER_NOT_TO_SAY: 'Prefer Not to Say',
  UNDER_25: 'Under 25',
  AGE_25_34: '25–34',
  AGE_35_44: '35–44',
  AGE_45_54: '45–54',
  AGE_55_PLUS: '55+',
  LIKERT_SCALE: 'Rating scale (1–5)',
  BINARY: 'Yes / No',
};

export function humanize(value: string | null | undefined): string {
  if (!value) return '';
  if (ENUM_LABEL_OVERRIDES[value]) return ENUM_LABEL_OVERRIDES[value];
  return value
    .toLowerCase()
    .split('_')
    .map((w) => (w ? w.charAt(0).toUpperCase() + w.slice(1) : w))
    .join(' ');
}

export function statusColor(value: string | null | undefined): string {
  switch (value) {
    case 'ACTIVE':
    case 'PUBLISHED':
    case 'CONFIRMED':
    case 'ACHIEVED':
    case 'RESOLVED':
    case 'CONSENTED':
    case 'COMPLETED':
    case 'IMPROVING':
    case 'READ':
      return 'success';
    case 'DRAFT':
    case 'PENDING':
    case 'PLANNED':
    case 'COMPUTED':
    case 'UNREAD':
    case 'STATIC':
      return 'secondary';
    case 'OFF_TRACK':
    case 'WORSENING':
    case 'OPEN':
    case 'DECLINED':
    case 'CANCELLED':
    case 'INACTIVE':
      return 'danger';
    case 'REMEDIATION_IN_PROGRESS':
    case 'CLOSED':
    case 'SUPERSEDED':
    case 'NOT_DISCLOSED':
    case 'DISMISSED':
      return 'warning';
    default:
      return 'info';
  }
}
