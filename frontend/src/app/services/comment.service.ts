import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { Comment, CommentRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class CommentService {
  private readonly http = inject(HttpClient);

  private base(projectId: string, taskId: string) {
    return `${environment.apiUrl}/projects/${projectId}/tasks/${taskId}/comments`;
  }

  getAll(projectId: string, taskId: string) {
    return this.http.get<Comment[]>(this.base(projectId, taskId));
  }

  create(projectId: string, taskId: string, request: CommentRequest) {
    return this.http.post<Comment>(this.base(projectId, taskId), request);
  }

  delete(projectId: string, taskId: string, commentId: string) {
    return this.http.delete<void>(`${this.base(projectId, taskId)}/${commentId}`);
  }
}
