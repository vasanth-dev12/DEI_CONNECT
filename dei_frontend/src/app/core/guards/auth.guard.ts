import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.hasValidToken()) {
    return true;
  }
  auth.logout(false);
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
