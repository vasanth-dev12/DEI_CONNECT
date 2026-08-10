import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { ErgFocus, ErgStatus } from '../models/enums';
import {
  CreateErgRequest,
  CreateEventRequest,
  ErgResponse,
  EventParticipationResponse,
  EventResponse,
  MembershipResponse,
  ParticipantResponse,
  UpdateErgRequest,
  UpdateEventRequest,
  UpdateMembershipRequest,
} from '../models/erg.model';

@Injectable({ providedIn: 'root' })
export class ErgService {
  private readonly http = inject(HttpClient);
  list(
    page: PageQuery,
    filters: { focus?: ErgFocus | null; status?: ErgStatus | null } = {},
  ): Observable<Page<ErgResponse>> {
    return this.http.get<Page<ErgResponse>>(apiUrl(API.ergs.base), {
      params: pageParams(page, { focus: filters.focus ?? undefined, status: filters.status ?? undefined }),
    });
  }

  getById(id: number): Observable<ErgResponse> {
    return this.http.get<ErgResponse>(apiUrl(API.ergs.byId(id)));
  }

  create(body: CreateErgRequest): Observable<ErgResponse> {
    return this.http.post<ErgResponse>(apiUrl(API.ergs.base), body);
  }

  update(id: number, body: UpdateErgRequest): Observable<ErgResponse> {
    return this.http.put<ErgResponse>(apiUrl(API.ergs.byId(id)), body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.ergs.byId(id)));
  }

  join(ergId: number): Observable<MembershipResponse> {
    return this.http.post<MembershipResponse>(apiUrl(API.ergs.memberships(ergId)), {});
  }

  leave(ergId: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.ergs.myMembership(ergId)));
  }

  myMembership(ergId: number): Observable<MembershipResponse> {
    return this.http.get<MembershipResponse>(apiUrl(API.ergs.myMembership(ergId)));
  }

  listMembers(ergId: number, page: PageQuery): Observable<Page<MembershipResponse>> {
    return this.http.get<Page<MembershipResponse>>(apiUrl(API.ergs.memberships(ergId)), {
      params: pageParams(page),
    });
  }

  updateMembership(ergId: number, membershipId: number, body: UpdateMembershipRequest): Observable<MembershipResponse> {
    return this.http.put<MembershipResponse>(apiUrl(API.ergs.membershipById(ergId, membershipId)), body);
  }

  listEvents(ergId: number, page: PageQuery): Observable<Page<EventResponse>> {
    return this.http.get<Page<EventResponse>>(apiUrl(API.ergs.events(ergId)), {
      params: pageParams(page),
    });
  }

  getEvent(ergId: number, eventId: number): Observable<EventResponse> {
    return this.http.get<EventResponse>(apiUrl(API.ergs.eventById(ergId, eventId)));
  }

  createEvent(ergId: number, body: CreateEventRequest): Observable<EventResponse> {
    return this.http.post<EventResponse>(apiUrl(API.ergs.events(ergId)), body);
  }

  updateEvent(ergId: number, eventId: number, body: UpdateEventRequest): Observable<EventResponse> {
    return this.http.put<EventResponse>(apiUrl(API.ergs.eventById(ergId, eventId)), body);
  }

  deleteEvent(ergId: number, eventId: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.ergs.eventById(ergId, eventId)));
  }

  participate(ergId: number, eventId: number): Observable<EventParticipationResponse> {
    return this.http.post<EventParticipationResponse>(apiUrl(API.ergs.participate(ergId, eventId)), {});
  }

  cancelParticipation(ergId: number, eventId: number): Observable<void> {
    return this.http.delete<void>(apiUrl(API.ergs.participate(ergId, eventId)));
  }

  listParticipants(ergId: number, eventId: number): Observable<ParticipantResponse[]> {
    return this.http.get<ParticipantResponse[]>(apiUrl(API.ergs.participants(ergId, eventId)));
  }
}
