import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';
import { Project, ProjectRequest, ProjectStatus } from '../models';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/projects`;

  getAll() {
    return this.http.get<Project[]>(this.base);
  }

  getById(id: string) {
    return this.http.get<Project>(`${this.base}/${id}`);
  }

  create(request: ProjectRequest) {
    return this.http.post<Project>(this.base, request);
  }

  updateStatus(id: string, status: ProjectStatus) {
    return this.http.patch<Project>(`${this.base}/${id}/status`, null, {
      params: { status },
    });
  }
}
