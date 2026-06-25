import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  constructor() {
    const auth = inject(AuthService);
    // Restore the current user from the stored token on every page load
    if (auth.isLoggedIn()) {
      auth.loadCurrentUser().pipe(takeUntilDestroyed()).subscribe();
    }
  }
}
