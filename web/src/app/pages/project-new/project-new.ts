import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmLabelImports } from '@spartan-ng/helm/label';
import { HlmTextareaImports } from '@spartan-ng/helm/textarea';
import { ProjectService } from '../../services/project.service';
import { ApiError } from '../../models/auth.model';
import { Visibility } from '../../models/project.model';

@Component({
  selector: 'app-project-new',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    HlmButtonImports,
    HlmCardImports,
    HlmTypographyImports,
    HlmInputImports,
    HlmLabelImports,
    HlmTextareaImports,
  ],
  templateUrl: './project-new.html',
})
export class ProjectNewComponent {
  private readonly fb = inject(FormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    visibility: ['PRIVATE' as Visibility, Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    const { name, description, visibility } = this.form.getRawValue();
    this.projectService.create({ name, description: description || undefined, visibility }).subscribe({
      next: (p) => void this.router.navigate(['/project', p.slug]),
      error: (err: unknown) => {
        this.loading.set(false);
        if (err instanceof HttpErrorResponse) {
          const body = err.error as ApiError;
          this.errorMessage.set(body?.error ?? 'Failed to create project.');
        }
      },
    });
  }
}
