import { Directive, Input, TemplateRef, ViewContainerRef, effect, inject } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { Role } from '../../core/models/enums';

@Directive(
  { selector: '[appHasRole]', standalone: true }
)
export class HasRoleDirective {
  private readonly auth = inject(AuthService);
  private readonly tpl = inject(TemplateRef<unknown>);
  private readonly vcr = inject(ViewContainerRef);

  private roles: Role[] = [];
  private rendered = false;

  constructor() {
    effect(() => {
      this.auth.currentUser();
      this.update();
    });
  }

  @Input() set appHasRole(roles: Role[] | Role | null | undefined) {
    this.roles = Array.isArray(roles) ? roles : roles ? [roles] : [];
    this.update();
  }

  private update(): void {
    const allowed = this.auth.hasAnyRole(this.roles);
    if (allowed && !this.rendered) {
      this.vcr.createEmbeddedView(this.tpl);
      this.rendered = true;
    } else if (!allowed && this.rendered) {
      this.vcr.clear();
      this.rendered = false;
    }
  }
}
