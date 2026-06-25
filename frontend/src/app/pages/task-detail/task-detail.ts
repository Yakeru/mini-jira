import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { concat, forkJoin, Observable } from 'rxjs';
import { last } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { TaskService } from '../../services/task.service';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';
import { Comment, Task, TaskPriority, TaskStatus } from '../../models';
import { StatusLabelPipe } from '../../pipes/status-label.pipe';

const ALLOWED_TRANSITIONS: Record<TaskStatus, TaskStatus[]> = {
  TODO:        ['IN_PROGRESS', 'BLOCKED'],
  IN_PROGRESS: ['TODO', 'DONE', 'BLOCKED'],
  BLOCKED:     ['TODO', 'IN_PROGRESS'],
  DONE:        [],
};

@Component({
  selector: 'app-task-detail',
  imports: [RouterLink, DatePipe, ReactiveFormsModule, StatusLabelPipe],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.scss',
})
export class TaskDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly taskService = inject(TaskService);
  private readonly commentService = inject(CommentService);
  private readonly destroyRef = inject(DestroyRef);
  readonly auth = inject(AuthService);

  private readonly projectId = this.route.snapshot.paramMap.get('id')!;
  private readonly taskId = this.route.snapshot.paramMap.get('taskId')!;

  readonly task = signal<Task | undefined>(undefined);
  readonly comments = signal<Comment[] | undefined>(undefined);
  readonly selectedStatus = signal<TaskStatus | undefined>(undefined);
  readonly selectedPriority = signal<TaskPriority | undefined>(undefined);
  readonly submitting = signal(false);

  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED'];
  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

  readonly hasPendingChange = computed(() =>
    this.selectedStatus() !== this.task()?.status ||
    this.selectedPriority() !== this.task()?.priority
  );

  readonly commentForm = inject(FormBuilder).group({
    content: ['', [Validators.required, Validators.maxLength(5000)]],
  });

  constructor() {
    forkJoin({
      task: this.taskService.getById(this.projectId, this.taskId),
      comments: this.commentService.getAll(this.projectId, this.taskId),
    })
      .pipe(takeUntilDestroyed())
      .subscribe(({ task, comments }) => {
        this.task.set(task);
        this.selectedStatus.set(task.status);
        this.selectedPriority.set(task.priority);
        this.comments.set(comments);
      });
  }

  onStatusChange(event: Event): void {
    this.selectedStatus.set((event.target as HTMLSelectElement).value as TaskStatus);
  }

  onPriorityChange(event: Event): void {
    this.selectedPriority.set((event.target as HTMLSelectElement).value as TaskPriority);
  }

  isStatusDisabled(status: TaskStatus): boolean {
    const current = this.task()?.status;
    if (!current) return true;
    if (status === current) return false;
    return !ALLOWED_TRANSITIONS[current].includes(status);
  }

  saveChanges(): void {
    if (!this.hasPendingChange() || this.submitting()) return;

    const calls: Observable<Task>[] = [];

    if (this.selectedStatus() !== this.task()?.status) {
      calls.push(this.taskService.updateStatus(this.projectId, this.taskId, this.selectedStatus()!));
    }
    if (this.selectedPriority() !== this.task()?.priority) {
      calls.push(this.taskService.updatePriority(this.projectId, this.taskId, this.selectedPriority()!));
    }

    this.submitting.set(true);

    // concat runs calls sequentially — avoids race conditions on the same entity.
    // last() gives us only the final updated task.
    concat(...calls)
      .pipe(last(), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: updated => {
          this.task.set(updated);
          this.selectedStatus.set(updated.status);
          this.selectedPriority.set(updated.priority);
          this.submitting.set(false);
        },
        error: () => this.submitting.set(false),
      });
  }

  submitComment(): void {
    if (this.commentForm.invalid) return;
    const content = this.commentForm.value.content!;
    this.commentService
      .create(this.projectId, this.taskId, { content })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(comment => {
        this.comments.update(list => [...(list ?? []), comment]);
        this.commentForm.reset();
      });
  }

  deleteComment(commentId: string): void {
    this.commentService
      .delete(this.projectId, this.taskId, commentId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.comments.update(list => list?.filter(c => c.id !== commentId));
      });
  }
}
