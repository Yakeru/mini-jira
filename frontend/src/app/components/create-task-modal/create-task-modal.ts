import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { TaskService } from '../../services/task.service';
import { Task, TaskPriority } from '../../models';

@Component({
  selector: 'app-create-task-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './create-task-modal.html',
  styleUrl: './create-task-modal.scss',
})
export class CreateTaskModal {
  private readonly taskService = inject(TaskService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  // Inputs — data flows in from the parent
  readonly projectId = input.required<string>();

  // Outputs — events flow out to the parent
  readonly taskCreated = output<Task>();
  readonly cancelled = output<void>();

  readonly submitting = signal(false);

  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

  readonly form = this.fb.group({
    title:       ['', [Validators.required, Validators.maxLength(200)]],
    description: ['',  Validators.maxLength(5000)],
    priority:    ['MEDIUM' as TaskPriority, Validators.required],
    dueDate:     [''],
  });

  onSubmit(): void {
    if (this.form.invalid) return;

    this.submitting.set(true);
    const { title, description, priority, dueDate } = this.form.value;

    this.taskService
      .create(this.projectId(), {
        title: title!,
        description: description ?? undefined,
        priority: priority as TaskPriority,
        dueDate: dueDate || undefined,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: task => {
          this.submitting.set(false);
          this.taskCreated.emit(task);   // ← sends the new task up to the parent
        },
        error: () => this.submitting.set(false),
      });
  }
}
