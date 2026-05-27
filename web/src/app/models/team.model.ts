export type TeamRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type TeamPlan = 'FREE' | 'PRO' | 'TEAM';

export interface TeamMember {
  userId: string;
  email: string;
  name: string;
  role: TeamRole;
  joinedAt: string;
}

export interface Team {
  id: string;
  name: string;
  plan: TeamPlan;
  members: TeamMember[];
}
