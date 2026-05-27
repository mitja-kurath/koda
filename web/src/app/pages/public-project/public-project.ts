import { Component, signal, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';
import { SpecViewerComponent } from '../../components/spec-viewer/spec-viewer';
import { MarkdownViewComponent } from '../../components/markdown-view/markdown-view';
import { ProjectService } from '../../services/project.service';
import { PageService } from '../../services/page.service';
import { Project } from '../../models/project.model';
import { Page } from '../../models/page.model';

type Tab = 'spec' | 'guides';

@Component({
  selector: 'app-public-project',
  imports: [
    RouterLink,
    HlmButtonImports,
    HlmCardImports,
    HlmTypographyImports,
    SpecViewerComponent,
    MarkdownViewComponent,
  ],
  templateUrl: './public-project.html',
})
export class PublicProjectComponent implements OnInit {
  protected readonly project = signal<Project | null>(null);
  protected readonly spec = signal<string | null>(null);
  protected readonly pages = signal<Page[]>([]);
  protected readonly selectedPage = signal<Page | null>(null);
  protected readonly activeTab = signal<Tab>('spec');
  protected readonly notFound = signal(false);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly projectService: ProjectService,
    private readonly pageService: PageService
  ) {}

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug')!;
    this.projectService.getPublic(slug).subscribe({
      next: (p) => {
        this.project.set(p);
        if (p.hasSpec) {
          this.projectService.getPublicSpec(slug).subscribe({ next: (s) => this.spec.set(s), error: () => {} });
        }
        this.pageService.listPublic(slug).subscribe({ next: (list) => this.pages.set(list), error: () => {} });
      },
      error: () => this.notFound.set(true),
    });
  }

  protected setTab(tab: Tab): void { this.activeTab.set(tab); }

  protected selectPage(page: Page): void { this.selectedPage.set(page); }
}
