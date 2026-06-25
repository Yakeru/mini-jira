import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { AuthCallback } from './pages/auth-callback/auth-callback';
import { Projects } from './pages/projects/projects';
import { ProjectDetail } from './pages/project-detail/project-detail';
import { TaskDetail } from './pages/task-detail/task-detail';
import { authGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login',                                component: Login,          canActivate: [guestGuard] },
  { path: 'auth/callback',                        component: AuthCallback                              },
  { path: 'projects',                             component: Projects,       canActivate: [authGuard]  },
  { path: 'projects/:id',                         component: ProjectDetail,  canActivate: [authGuard]  },
  { path: 'projects/:id/tasks/:taskId',           component: TaskDetail,     canActivate: [authGuard]  },
  { path: '',                                     redirectTo: 'projects',    pathMatch: 'full'         },
];
