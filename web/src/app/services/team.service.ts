import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Team, TeamMember } from '../models/team.model';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly base = `${environment.apiUrl}/teams`;

  constructor(private readonly http: HttpClient) {}

  getMyTeam(): Observable<Team> {
    return this.http.get<Team>(`${this.base}/me`);
  }

  getMembers(): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${this.base}/me/members`);
  }

  invite(email: string): Observable<TeamMember> {
    return this.http.post<TeamMember>(`${this.base}/me/members`, { email });
  }

  removeMember(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/me/members/${userId}`);
  }
}
