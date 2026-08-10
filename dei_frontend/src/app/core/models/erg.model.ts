import {
  ErgFocus,
  ErgStatus,
  EventStatus,
  EventType,
  MembershipRole,
  MembershipStatus,
} from './enums';

export interface CreateErgRequest {
  ergName: string;
  focus: ErgFocus;
  mission?: string | null;
  ergLeadId: number;
  executiveSponsorId?: number | null;
  foundedDate?: string | null;
}

export interface UpdateErgRequest {
  ergName: string;
  focus: ErgFocus;
  mission?: string | null;
  ergLeadId: number;
  executiveSponsorId?: number | null;
  foundedDate?: string | null;
  status: ErgStatus;
}

export interface ErgResponse {
  ergId: number;
  ergName: string;
  focus: ErgFocus;
  mission: string | null;
  executiveSponsorId: number | null;
  ergLeadId: number;
  memberCount: number;
  foundedDate: string | null;
  status: ErgStatus;
  createdDate: string;
  lastModifiedDate: string;
  creatorManagerId: number | null;
  creatorManagerName: string | null;
}

export interface CreateEventRequest {
  eventName: string;
  eventType: EventType;
  date: string;
  attendeeCount?: number | null;
  budgetSpent?: number | null;
}

export interface UpdateEventRequest {
  eventName: string;
  eventType: EventType;
  date: string;
  attendeeCount?: number | null;
  budgetSpent?: number | null;
  status: EventStatus;
}

export interface EventResponse {
  eventId: number;
  ergId: number;
  eventName: string;
  eventType: EventType;
  date: string;
  attendeeCount: number | null;
  budgetSpent: number | null;
  status: EventStatus;
  createdDate: string;
  lastModifiedDate: string;
}

export interface EventParticipationResponse {
  participationId: number;
  eventId: number;
  eventName: string;
  employeeId: number;
  employeeName: string;
  registrationDate: string;
}

export interface ParticipantResponse {
  employeeId: number;
  employeeName: string;
  employeeEmail: string;
  registrationDate: string;
}

export interface UpdateMembershipRequest {
  role: MembershipRole;
  status: MembershipStatus;
}

export interface MembershipResponse {
  membershipId: number;
  ergId: number;
  employeeUserId: number;
  employeeId: string;
  role: MembershipRole;
  joinDate: string;
  status: MembershipStatus;
  createdDate: string;
}
