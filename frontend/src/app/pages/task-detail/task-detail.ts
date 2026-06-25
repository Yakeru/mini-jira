import { Component, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { TaskService } from '../../services/task.service';
import { CommentService } from '../../services/comment.service';
import { AuthService } from '../../services/auth.service';
import { Comment, Task, TaskStatus } from '../../models';
import { StatusLabelPipe } from '../../pipes/status-label.pipe';

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

  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED'];

  readonly commentForm = inject(FormBuilder).group({
    content: ['', [Validators.required, Validators.maxLength(5000)]],
  });

  constructor() {
    // forkJoin fires both requests in parallel and emits once when both complete
    forkJoin({
      task: this.taskService.getById(this.projectId, this.taskId),
      comments: this.commentService.getAll(this.projectId, this.taskId),
    })
      .pipe(takeUntilDestroyed())
      .subscribe(({ task, comments }) => {
        this.task.set(task);
        this.comments.set(comments);
      });
  }

  onStatusChange(event: Event): void {
    const status = (event.target as HTMLSelectElement).value as TaskStatus;
    this.taskService
      .updateStatus(this.projectId, this.taskId, status)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(updated => this.task.set(updated));
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
