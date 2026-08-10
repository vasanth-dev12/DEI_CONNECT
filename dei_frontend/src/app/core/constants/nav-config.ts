import { Role } from '../models/enums';

export interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: Role[];
}

export const NAV_ITEMS: NavItem[] = [
  {
    label: 'Dashboard',
    icon: 'bi-speedometer2',
    route: '/dashboard',
    roles: ['EMPLOYEE', 'DEI_MANAGER', 'HR_BIZ_PARTNER', 'ERG_LEAD', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'Users',
    icon: 'bi-people',
    route: '/users',
    roles: ['DEI_MANAGER', 'HR_BIZ_PARTNER', 'ERG_LEAD', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'Surveys',
    icon: 'bi-clipboard-check',
    route: '/surveys',
    roles: ['EMPLOYEE', 'DEI_MANAGER', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'My Demographics',
    icon: 'bi-person-badge',
    route: '/diversity/my-profile',
    roles: ['EMPLOYEE'],
  },
  {
    label: 'Representation',
    icon: 'bi-bar-chart',
    route: '/diversity/snapshots',
    roles: ['DEI_MANAGER', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'Goals',
    icon: 'bi-bullseye',
    route: '/goals',
    roles: ['DEI_MANAGER', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'Pay Equity',
    icon: 'bi-cash-coin',
    route: '/pay-equity/analyses',
    roles: ['HR_BIZ_PARTNER', 'ADMIN'],
  },
  {
    label: 'Pay Equity (Published)',
    icon: 'bi-file-earmark-bar-graph',
    route: '/pay-equity/published',
    roles: ['DEI_MANAGER', 'EXECUTIVE', 'HR_BIZ_PARTNER', 'ADMIN'],
  },
  {
    label: 'ERG Groups',
    icon: 'bi-diagram-3',
    route: '/ergs',
    roles: ['EMPLOYEE', 'DEI_MANAGER', 'HR_BIZ_PARTNER', 'ERG_LEAD', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'Reports',
    icon: 'bi-graph-up',
    route: '/reports',
    roles: ['DEI_MANAGER', 'HR_BIZ_PARTNER', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'Notifications',
    icon: 'bi-bell',
    route: '/notifications',
    roles: ['EMPLOYEE', 'DEI_MANAGER', 'HR_BIZ_PARTNER', 'ERG_LEAD', 'EXECUTIVE', 'ADMIN'],
  },
  {
    label: 'Audit Logs',
    icon: 'bi-shield-lock',
    route: '/audit-logs',
    roles: ['ADMIN'],
  },
];
