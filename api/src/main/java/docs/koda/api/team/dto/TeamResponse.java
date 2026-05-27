package docs.koda.api.team.dto;

import docs.koda.api.user.domain.Team;

import java.util.List;
import java.util.UUID;

public record TeamResponse(UUID id, String name, Team.Plan plan, List<TeamMemberResponse> members) {}
