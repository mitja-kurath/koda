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
import { SpecViewerComponent } from '../../components/spec-viewer/spec-viewer';
import { ProjectService } from '../../services/project.service';
import { PageService } from '../../services/page.service';
import { Project } from '../../models/project.model';
import { Page } from '../../models/page.model';
import { ApiError } from '../../models/auth.model';

type Tab = 'spec' | 'guides' | 'settings';

@Component({
  selector: 'app-project',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    HlmButtonImports,
    HlmCardImports,
    HlmTypographyImports,
    HlmInputImports,
    HlmLabelImports,
    HlmTextareaImports,
    SpecViewerComponent,
  ],
  templateUrl: './project.html',
})
export class ProjectComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly pageService = inject(PageService);

  protected readonly project = signal<Project | null>(null);
  protected readonly pages = signal<Page[]>([]);
  protected readonly spec = signal<string | null>(null);
  protected readonly activeTab = signal<Tab>('spec');
  protected readonly loading = signal(true);
  protected readonly specLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly settingsError = signal<string | null>(null);
  protected readonly deleteConfirm = signal(false);

  protected readonly specForm = this.fb.nonNullable.group({
    content: ['', Validators.required],
  });

  protected readonly settingsForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    visibility: ['PRIVATE'],
  });

  protected readonly publicUrl = computed(() => {
    const p = this.project();
    return p ? `${window.location.origin}/p/${p.slug}` : '';
  });

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug')!;
    this.projectService.get(slug).subscribe({
      next: (p) => {
        this.project.set(p);
        this.settingsForm.patchValue({ name: p.name, description: p.description ?? '', visibility: p.visibility });
        this.loading.set(false);
        if (p.hasSpec) this.loadSpec(slug);
        this.loadPages(slug);
      },
      error: () => { this.loading.set(false); void this.router.navigate(['/dashboard']); },
    });
  }

  protected setTab(tab: Tab): void { this.activeTab.set(tab); }

  protected uploadSpec(): void {
    if (this.specForm.invalid || this.specLoading()) return;
    this.specLoading.set(true);
    this.errorMessage.set(null);
    const slug = this.project()!.slug;
    this.projectService.uploadSpec(slug, this.specForm.getRawValue().content).subscribe({
      next: (p) => {
        this.project.set(p);
        this.specLoading.set(false);
        this.specForm.reset();
        this.loadSpec(slug);
      },
      error: (err: unknown) => {
        this.specLoading.set(false);
        if (err instanceof HttpErrorResponse) {
          const body = err.error as ApiError;
          this.errorMessage.set(body?.error ?? 'Failed to upload spec.');
        }
      },
    });
  }

  protected saveSettings(): void {
    if (this.settingsForm.invalid) return;
    const slug = this.project()!.slug;
    const { name, description, visibility } = this.settingsForm.getRawValue();
    this.projectService.update(slug, { name, description: description || undefined, visibility: visibility as 'PUBLIC' | 'PRIVATE' }).subscribe({
      next: (p) => { this.project.set(p); this.settingsError.set(null); },
      error: (err: unknown) => {
        if (err instanceof HttpErrorResponse) {
          const body = err.error as ApiError;
          this.settingsError.set(body?.error ?? 'Failed to save settings.');
        }
      },
    });
  }

  protected confirmDelete(): void { this.deleteConfirm.set(true); }

  protected deleteProject(): void {
    const slug = this.project()!.slug;
    this.projectService.delete(slug).subscribe({
      next: () => void this.router.navigate(['/dashboard']),
      error: () => {},
    });
  }

  protected deletePage(pageSlug: string): void {
    const slug = this.project()!.slug;
    this.pageService.delete(slug, pageSlug).subscribe({
      next: () => this.pages.update((list) => list.filter((p) => p.slug !== pageSlug)),
      error: () => {},
    });
  }

  private loadSpec(slug: string): void {
    this.projectService.getSpec(slug).subscribe({ next: (s) => this.spec.set(s), error: () => {} });
  }

  private loadPages(slug: string): void {
    this.pageService.list(slug).subscribe({ next: (list) => this.pages.set(list), error: () => {} });
  }
}
