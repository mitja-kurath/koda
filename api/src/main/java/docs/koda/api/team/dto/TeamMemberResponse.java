package docs.koda.api.team.dto;

import docs.koda.api.team.domain.TeamMember.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TeamMemberResponse(UUID userId, String email, String name, Role role, OffsetDateTime joinedAt) {}
