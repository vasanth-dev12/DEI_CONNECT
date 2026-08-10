import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';
import { ToastService } from '../services/toast.service';
import { ApiError } from '../models/common.model';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const body = err.error as ApiError | undefined;
      const message = body?.message || err.message || 'Unexpected error';

      switch (err.status) {
        case 0:
          toast.error('Cannot reach the API Gateway. Is it running on the configured URL?');
          break;
        case 401:
          auth.logout(false);
          router.navigate(['/login']);
          toast.warning('Your session has expired. Please sign in again.');
          break;
        case 403:
          toast.error(message || 'You do not have permission to perform that action.');
          break;
        case 400:
          toast.error(body?.fieldErrors?.length ? 'Please correct the highlighted fields.' : message);
          break;
        case 404:
          toast.error(message || 'Not found.');
          break;
        case 409:
          toast.error(message || 'Conflict.');
          break;
        case 422:
          toast.error(message || 'Request could not be processed.');
          break;
        default:
          if (err.status >= 500) {
            toast.error('A server error occurred. Please try again later.');
          } else {
            toast.error(message);
          }
      }
      return throwError(() => err);
    }),
  );
};
