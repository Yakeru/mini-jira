import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ProjectService } from '../../services/project.service';
import { Project } from '../../models';

@Component({
  selector: 'app-projects',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './projects.html',
  styleUrl: './projects.scss',
})
export class Projects {
  private readonly projectService = inject(ProjectService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly projects = signal<Project[] | undefined>(undefined);
  readonly showModal = signal(false);
  readonly submitting = signal(false);

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(500)],
  });

  constructor() {
    this.projectService.getAll()
      .pipe(takeUntilDestroyed())  // no argument needed here — we're in the constructor
      .subscribe(projects => this.projects.set(projects));
  }

  openModal(): void {
    this.form.reset();
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.submitting.set(true);

    const { name, description } = this.form.value;

    this.projectService.create({ name: name!, description: description ?? undefined })
      .pipe(takeUntilDestroyed(this.destroyRef))  // explicit DestroyRef required outside constructor
      .subscribe({
        next: project => {
          this.projects.update(list => [project, ...(list ?? [])]);
          this.submitting.set(false);
          this.closeModal();
        },
        error: () => {
          this.submitting.set(false);
        },
      });
  }
}
