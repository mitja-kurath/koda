import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Project, CreateProjectRequest, UpdateProjectRequest } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly base = `${environment.apiUrl}/projects`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<Project[]> {
    return this.http.get<Project[]>(this.base);
  }

  get(slug: string): Observable<Project> {
    return this.http.get<Project>(`${this.base}/${slug}`);
  }

  create(req: CreateProjectRequest): Observable<Project> {
    return this.http.post<Project>(this.base, req);
  }

  update(slug: string, req: UpdateProjectRequest): Observable<Project> {
    return this.http.put<Project>(`${this.base}/${slug}`, req);
  }

  delete(slug: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${slug}`);
  }

  uploadSpec(slug: string, content: string): Observable<Project> {
    return this.http.post<Project>(`${this.base}/${slug}/spec`, { content });
  }

  getSpec(slug: string): Observable<string> {
    return this.http.get(`${this.base}/${slug}/spec`, { responseType: 'text' });
  }

  getPublic(slug: string): Observable<Project> {
    return this.http.get<Project>(`${environment.apiUrl}/public/${slug}`);
  }

  getPublicSpec(slug: string): Observable<string> {
    return this.http.get(`${environment.apiUrl}/public/${slug}/spec`, { responseType: 'text' });
  }
}
