import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page, PageRequest } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class PageService {
  constructor(private readonly http: HttpClient) {}

  private base(projectSlug: string): string {
    return `${environment.apiUrl}/projects/${projectSlug}/pages`;
  }

  list(projectSlug: string): Observable<Page[]> {
    return this.http.get<Page[]>(this.base(projectSlug));
  }

  get(projectSlug: string, pageSlug: string): Observable<Page> {
    return this.http.get<Page>(`${this.base(projectSlug)}/${pageSlug}`);
  }

  create(projectSlug: string, req: PageRequest): Observable<Page> {
    return this.http.post<Page>(this.base(projectSlug), req);
  }

  update(projectSlug: string, pageSlug: string, req: PageRequest): Observable<Page> {
    return this.http.put<Page>(`${this.base(projectSlug)}/${pageSlug}`, req);
  }

  delete(projectSlug: string, pageSlug: string): Observable<void> {
    return this.http.delete<void>(`${this.base(projectSlug)}/${pageSlug}`);
  }

  listPublic(projectSlug: string): Observable<Page[]> {
    return this.http.get<Page[]>(`${environment.apiUrl}/public/${projectSlug}/pages`);
  }

  getPublic(projectSlug: string, pageSlug: string): Observable<Page> {
    return this.http.get<Page>(`${environment.apiUrl}/public/${projectSlug}/pages/${pageSlug}`);
  }
}
