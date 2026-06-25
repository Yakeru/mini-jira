import { Pipe, PipeTransform } from '@angular/core';

const LABELS: Record<string, string> = {
  TODO:        'To Do',
  IN_PROGRESS: 'In Progress',
  DONE:        'Done',
  BLOCKED:     'Blocked',
  ACTIVE:      'Active',
  ARCHIVED:    'Archived',
  COMPLETED:   'Completed',
};

@Pipe({ name: 'statusLabel' })
export class StatusLabelPipe implements PipeTransform {
  transform(value: string): string {
    return LABELS[value] ?? value;
  }
}
