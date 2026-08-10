import { Pipe, PipeTransform } from '@angular/core';
import { roleLabel } from '../../core/constants/labels';
import { Role } from '../../core/models/enums';

@Pipe({ name: 'roleLabel', standalone: true })
export class RoleLabelPipe implements PipeTransform {
  transform(value: Role | string | null | undefined): string {
    return roleLabel(value);
  }
}
