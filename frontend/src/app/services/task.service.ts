import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { Task, TaskPriority, TaskRequest, TaskStatus } from '../models';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);

  private base(projectId: string) {
    return `${environment.apiUrl}/projects/${projectId}/tasks`;
  }

  getAll(projectId: string) {
    return this.http.get<Task[]>(this.base(projectId));
  }

  getById(projectId: string, taskId: string) {
    return this.http.get<Task>(`${this.base(projectId)}/${taskId}`);
  }

  create(projectId: string, request: TaskRequest) {
    return this.http.post<Task>(this.base(projectId), request);
  }

  updateStatus(projectId: string, taskId: string, status: TaskStatus) {
    return this.http.patch<Task>(`${this.base(projectId)}/${taskId}/status`, null, {
      params: { status },
    });
  }

  updatePriority(projectId: string, taskId: string, priority: TaskPriority) {
    return this.http.patch<Task>(`${this.base(projectId)}/${taskId}/priority`, null, {
      params: { priority },
    });
  }
}
