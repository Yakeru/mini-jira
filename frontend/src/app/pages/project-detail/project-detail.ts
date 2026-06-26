import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ProjectService } from '../../services/project.service';
import { TaskService } from '../../services/task.service';
import { TaskCard } from '../../components/task-card/task-card';
import { CreateTaskModal } from '../../components/create-task-modal/create-task-modal';
import { Project, Task, TaskStatus } from '../../models';

interface Column {
  status: TaskStatus;
  label: string;
}

@Component({
  selector: 'app-project-detail',
  imports: [RouterLink, TaskCard, CreateTaskModal],
  templateUrl: './project-detail.html',
  styleUrl: './project-detail.scss',
})
export class ProjectDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly projectService = inject(ProjectService);
  private readonly taskService = inject(TaskService);
  private readonly destroyRef = inject(DestroyRef);

  readonly projectId = this.route.snapshot.paramMap.get('id')!;

  readonly project = signal<Project | undefined>(undefined);
  readonly tasks = signal<Task[] | undefined>(undefined);
  readonly showTaskModal = signal(false);

  readonly columns: Column[] = [
    { status: 'TODO',        label: 'To Do'      },
    { status: 'IN_PROGRESS', label: 'In Progress' },
    { status: 'DONE',        label: 'Done'        },
    { status: 'BLOCKED',     label: 'Blocked'     },
  ];

  readonly tasksByStatus = computed(() => {
    const all = this.tasks() ?? [];
    const map: Record<TaskStatus, Task[]> = {
      TODO: [], IN_PROGRESS: [], DONE: [], BLOCKED: [],
    };
    for (const task of all) {
      map[task.status].push(task);
    }
    return map;
  });

  constructor() {
    this.projectService.getById(this.projectId)
      .pipe(takeUntilDestroyed())
      .subscribe(p => this.project.set(p));

    this.taskService.getAll(this.projectId)
      .pipe(takeUntilDestroyed())
      .subscribe(t => this.tasks.set(t));
  }

  onTaskCreated(task: Task): void {
    this.tasks.update(list => [...(list ?? []), task]);
    this.showTaskModal.set(false);
  }
}
