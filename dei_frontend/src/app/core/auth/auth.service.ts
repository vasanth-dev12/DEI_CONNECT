import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { API } from '../constants/api-paths';
import { AuthResponse, CurrentUser, LoginRequest } from '../models/iam.model';
import { Role } from '../models/enums';
import { isTokenExpired } from './jwt.util';

const TOKEN_KEY = 'dei.token';
const USER_KEY = 'dei.user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _user = signal<CurrentUser | null>(this.readStoredUser());
  private _token: string | null = localStorage.getItem(TOKEN_KEY);

  readonly currentUser = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null && !isTokenExpired(this._token));
  readonly role = computed<Role | null>(() => this._user()?.role ?? null);

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(environment.apiBaseUrl + API.auth.login, credentials)
      .pipe(tap((response) => this.persistSession(response)));
  }

  logout(redirect = true): void {
    this._token = null;
    this._user.set(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    if (redirect) {
      this.router.navigate(['/login']);
    }
  }

  syncCurrentUser(patch: Partial<CurrentUser>): void {
    const current = this._user();
    if (!current) {
      return;
    }
    const updatedUser: CurrentUser = { ...current, ...patch };
    this._user.set(updatedUser);
    localStorage.setItem(USER_KEY, JSON.stringify(updatedUser));
  }

  getToken(): string | null {
    return this._token;
  }

  hasValidToken(): boolean {
    return !!this._token && !isTokenExpired(this._token);
  }

  hasAnyRole(roles: Role[]): boolean {
    const currentRole = this.role();
    return !!currentRole && (roles.length === 0 || roles.includes(currentRole));
  }

  private persistSession(response: AuthResponse): void {
    this._token = response.token;
    const user: CurrentUser = {
      userId: response.userId,
      employeeId: response.employeeId,
      name: response.name,
      email: response.email,
      role: response.role,
    };
    this._user.set(user);
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  private readStoredUser(): CurrentUser | null {
    const storedUserJson = localStorage.getItem(USER_KEY);
    const storedToken = localStorage.getItem(TOKEN_KEY);
    if (!storedUserJson || !storedToken || isTokenExpired(storedToken)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      return null;
    }
    try {
      return JSON.parse(storedUserJson) as CurrentUser;
    } catch {
      return null;
    }
  }
}
