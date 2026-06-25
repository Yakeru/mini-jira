import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import { User } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'jwt_token';

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  // Writable signal — only this service can write to it
  private readonly _token = signal<string | null>(localStorage.getItem(this.TOKEN_KEY));
  private readonly _currentUser = signal<User | null>(null);

  // Public read-only views exposed to the rest of the app
  readonly isLoggedIn = computed(() => this._token() !== null);
  readonly currentUser = this._currentUser.asReadonly();

  getToken(): string | null {
    return this._token();
  }

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this._token.set(token);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this._token.set(null);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  loginWith(provider: string): void {
    window.location.href = `${environment.authUrl}/oauth2/authorization/${provider}`;
  }

  loadCurrentUser() {
    return this.http
      .get<User>(`${environment.apiUrl}/users/me`)
      .pipe(tap(user => this._currentUser.set(user)));
  }
}
