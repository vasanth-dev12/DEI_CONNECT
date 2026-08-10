import { Pipe, PipeTransform } from '@angular/core';
import { humanize } from '../../core/constants/labels';

@Pipe({ name: 'enumLabel', standalone: true })
export class EnumLabelPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    return humanize(value);
  }
}
