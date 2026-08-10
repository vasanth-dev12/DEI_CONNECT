import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';

import { LoadingService } from '../services/loading.service';
import { API } from '../constants/api-paths';

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(LoadingService);
  const silent = req.url.includes(API.notifications.unreadCount);

  if (!silent) {
    loading.start();
  }

  return next(req).pipe(
    finalize(() => {
      if (!silent) {
        loading.stop();
      }
    })
  );
};