import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API } from '../constants/api-paths';
import { apiUrl, pageParams } from './http-util';
import { Page, PageQuery } from '../models/common.model';
import { NotificationStatus } from '../models/enums';
import { EmitNotificationRequest, NotificationResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);

  readonly unread = signal(0);

  list(page: PageQuery, status?: NotificationStatus | null): Observable<Page<NotificationResponse>> {
    return this.http.get<Page<NotificationResponse>>(apiUrl(API.notifications.base), {
      params: pageParams(page, { status: status ?? undefined }),
    });
  }

  refreshUnreadCount(): Observable<number> {
    return this.http
      .get<number>(apiUrl(API.notifications.unreadCount))
      .pipe(tap((count) => this.unread.set(count ?? 0)));
  }

  getById(id: number): Observable<NotificationResponse> {
    return this.http.get<NotificationResponse>(apiUrl(API.notifications.byId(id)));
  }

  markRead(id: number): Observable<NotificationResponse> {
    return this.http
      .put<NotificationResponse>(apiUrl(API.notifications.read(id)), {})
      .pipe(tap(() => this.refreshUnreadCount().subscribe()));
  }

  dismiss(id: number): Observable<NotificationResponse> {
    return this.http
      .put<NotificationResponse>(apiUrl(API.notifications.dismiss(id)), {})
      .pipe(tap(() => this.refreshUnreadCount().subscribe()));
  }

  markAllRead(): Observable<number> {
    return this.http
      .put<number>(apiUrl(API.notifications.readAll), {})
      .pipe(tap(() => this.unread.set(0)));
  }

  emit(body: EmitNotificationRequest): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(apiUrl(API.notifications.emit), body);
  }
}
