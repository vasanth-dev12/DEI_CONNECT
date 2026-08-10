import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { Role } from '../models/enums';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const roles = (route.data?.['roles'] as Role[] | undefined) ?? [];
  if (auth.hasAnyRole(roles)) {
    return true;
  }
  return router.createUrlTree(['/access-denied']);
};
