import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { API } from '../constants/api-paths';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  const isLogin = req.url.includes(API.auth.login);
  if (token && !isLogin) {
    req = req.clone({
      setHeaders: { 
        Authorization: `Bearer ${token}` 
      },
    });
  }
  return next(req);
};
