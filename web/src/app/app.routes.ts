import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginComponent),
    canActivate: [guestGuard],
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register').then((m) => m.RegisterComponent),
    canActivate: [guestGuard],
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'team',
    loadComponent: () => import('./pages/team/team').then((m) => m.TeamComponent),
    canActivate: [authGuard],
  },
  {
    path: 'project/new',
    loadComponent: () => import('./pages/project-new/project-new').then((m) => m.ProjectNewComponent),
    canActivate: [authGuard],
  },
  {
    path: 'project/:slug',
    loadComponent: () => import('./pages/project/project').then((m) => m.ProjectComponent),
    canActivate: [authGuard],
  },
  {
    path: 'project/:slug/pages/:pageSlug',
    loadComponent: () => import('./pages/page-editor/page-editor').then((m) => m.PageEditorComponent),
    canActivate: [authGuard],
  },
  {
    path: 'p/:slug',
    loadComponent: () => import('./pages/public-project/public-project').then((m) => m.PublicProjectComponent),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
