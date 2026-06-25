import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Task } from '../../models';

@Component({
  selector: 'app-task-card',
  imports: [RouterLink],
  templateUrl: './task-card.html',
  styleUrl: './task-card.scss',
})
export class TaskCard {
  readonly task = input.required<Task>();
}
