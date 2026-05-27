export interface Page {
  id: string;
  projectId: string;
  title: string;
  slug: string;
  content: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface PageRequest {
  title: string;
  content: string;
  sortOrder: number;
}
