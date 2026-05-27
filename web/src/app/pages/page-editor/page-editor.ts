import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmLabelImports } from '@spartan-ng/helm/label';
import { HlmTextareaImports } from '@spartan-ng/helm/textarea';
import { MarkdownViewComponent } from '../../components/markdown-view/markdown-view';
import { PageService } from '../../services/page.service';
import { ApiError } from '../../models/auth.model';

@Component({
  selector: 'app-page-editor',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    HlmButtonImports,
    HlmCardImports,
    HlmTypographyImports,
    HlmInputImports,
    HlmLabelImports,
    HlmTextareaImports,
    MarkdownViewComponent,
  ],
  templateUrl: './page-editor.html',
})
export class PageEditorComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly pageService = inject(PageService);

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly showPreview = signal(false);
  protected readonly isNew = signal(true);
  protected projectSlug = '';
  protected pageSlug = '';

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    content: ['', Validators.required],
    sortOrder: [0],
  });

  protected readonly previewContent = computed(() => this.form.getRawValue().content);

  ngOnInit(): void {
    this.projectSlug = this.route.snapshot.paramMap.get('slug')!;
    const ps = this.route.snapshot.paramMap.get('pageSlug');
    if (ps && ps !== 'new') {
      this.isNew.set(false);
      this.pageSlug = ps;
      this.loading.set(true);
      this.pageService.get(this.projectSlug, ps).subscribe({
        next: (page) => {
          this.form.patchValue({ title: page.title, content: page.content, sortOrder: page.sortOrder });
          this.loading.set(false);
        },
        error: () => { this.loading.set(false); void this.router.navigate(['/project', this.projectSlug]); },
      });
    }
  }

  protected save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.errorMessage.set(null);
    const req = this.form.getRawValue();
    const action = this.isNew()
      ? this.pageService.create(this.projectSlug, req)
      : this.pageService.update(this.projectSlug, this.pageSlug, req);
    action.subscribe({
      next: () => void this.router.navigate(['/project', this.projectSlug]),
      error: (err: unknown) => {
        this.saving.set(false);
        if (err instanceof HttpErrorResponse) {
          const body = err.error as ApiError;
          this.errorMessage.set(body?.error ?? 'Failed to save page.');
        }
      },
    });
  }
}
