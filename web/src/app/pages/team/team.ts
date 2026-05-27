import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmTypographyImports } from '@spartan-ng/helm/typography';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmLabelImports } from '@spartan-ng/helm/label';
import { TeamService } from '../../services/team.service';
import { Team, TeamMember } from '../../models/team.model';
import { ApiError } from '../../models/auth.model';

@Component({
  selector: 'app-team',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    HlmButtonImports,
    HlmCardImports,
    HlmTypographyImports,
    HlmInputImports,
    HlmLabelImports,
  ],
  templateUrl: './team.html',
})
export class TeamComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly teamService = inject(TeamService);

  protected readonly team = signal<Team | null>(null);
  protected readonly loading = signal(true);
  protected readonly inviting = signal(false);
  protected readonly inviteError = signal<string | null>(null);
  protected readonly inviteSuccess = signal<string | null>(null);

  protected readonly inviteForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  ngOnInit(): void {
    this.teamService.getMyTeam().subscribe({
      next: (t) => { this.team.set(t); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  protected invite(): void {
    if (this.inviteForm.invalid || this.inviting()) return;
    this.inviting.set(true);
    this.inviteError.set(null);
    this.inviteSuccess.set(null);
    this.teamService.invite(this.inviteForm.getRawValue().email).subscribe({
      next: (member: TeamMember) => {
        this.inviting.set(false);
        this.inviteSuccess.set(`${member.name} has been added to the team.`);
        this.inviteForm.reset();
        this.team.update((t) => t ? { ...t, members: [...t.members, member] } : t);
      },
      error: (err: unknown) => {
        this.inviting.set(false);
        if (err instanceof HttpErrorResponse) {
          const body = err.error as ApiError;
          this.inviteError.set(body?.error ?? 'Failed to invite member.');
        }
      },
    });
  }

  protected removeMember(userId: string): void {
    this.teamService.removeMember(userId).subscribe({
      next: () => this.team.update((t) => t ? { ...t, members: t.members.filter((m) => m.userId !== userId) } : t),
      error: () => {},
    });
  }
}
