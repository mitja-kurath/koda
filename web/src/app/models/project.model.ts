export type Visibility = 'PUBLIC' | 'PRIVATE';

export interface Project {
  id: string;
  name: string;
  slug: string;
  description: string | null;
  visibility: Visibility;
  hasSpec: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
  visibility: Visibility;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  visibility?: Visibility;
}
