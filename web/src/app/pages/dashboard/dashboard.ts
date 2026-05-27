import { Component, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { AuthService } from '../../services/auth.service';
import { ProjectService } from '../../services/project.service';
import { Project } from '../../models/project.model';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, HlmButtonImports, HlmTypographyImports, HlmCardImports, HlmBadgeImports],
  templateUrl: './dashboard.html',
})
export class DashboardComponent implements OnInit {
  protected readonly projects = signal<Project[]>([]);
  protected readonly loading = signal(true);

  constructor(
    protected readonly authService: AuthService,
    private readonly projectService: ProjectService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.projectService.list().subscribe({
      next: (list) => { this.projects.set(list); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  protected logout(): void {
    this.authService.logout();
  }

  protected openProject(slug: string): void {
    void this.router.navigate(['/project', slug]);
  }
}
