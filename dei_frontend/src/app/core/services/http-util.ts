import { HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { PageQuery } from '../models/common.model';

export function apiUrl(path: string): string {
  return environment.apiBaseUrl + path;
}

export function toParams(obj: Record<string, unknown> = {}): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(obj)) {
    if (value === null || value === undefined || value === '') continue;
    params = params.set(key, String(value));
  }
  return params;
}

export function pageParams(page: PageQuery = {}, filters: Record<string, unknown> = {}): HttpParams {
  return toParams({
    page: page.page ?? 0,
    size: page.size ?? 10,
    sort: page.sort,
    ...filters,
  });
}
