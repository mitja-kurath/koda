package docs.koda.api.team.service;

import docs.koda.api.team.domain.TeamMember;
import docs.koda.api.team.domain.TeamMember.Role;
import docs.koda.api.team.dto.TeamMemberResponse;
import docs.koda.api.team.dto.TeamResponse;
import docs.koda.api.team.repository.TeamMemberRepository;
import docs.koda.api.user.domain.Team;
import docs.koda.api.user.domain.User;
import docs.koda.api.user.repository.TeamRepository;
import docs.koda.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamResponse getMyTeam(User user) {
        Team team = requireTeam(user);
        List<TeamMemberResponse> members = buildMemberList(team.getId());
        return new TeamResponse(team.getId(), team.getName(), team.getPlan(), members);
    }

    public List<TeamMemberResponse> getMembers(User user) {
        Team team = requireTeam(user);
        return buildMemberList(team.getId());
    }

    @Transactional
    public TeamMemberResponse invite(User inviter, String email) {
        Team team = requireTeam(inviter);
        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No account found with that email address"));
        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), invitee.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member");
        }
        TeamMember member = TeamMember.builder()
                .teamId(team.getId())
                .userId(invitee.getId())
                .role(Role.MEMBER)
                .build();
        teamMemberRepository.save(member);
        return new TeamMemberResponse(invitee.getId(), invitee.getEmail(), invitee.getName(),
                Role.MEMBER, member.getCreatedAt());
    }

    @Transactional
    public void removeMember(User owner, UUID targetUserId) {
        Team team = requireTeam(owner);
        if (team.getOwnerId().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the team owner");
        }
        teamMemberRepository.findByTeamIdAndUserId(team.getId(), targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        teamMemberRepository.deleteByTeamIdAndUserId(team.getId(), targetUserId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Team requireTeam(User user) {
        return teamRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Team not found"));
    }

    private List<TeamMemberResponse> buildMemberList(UUID teamId) {
        return teamMemberRepository.findAllByTeamId(teamId).stream()
                .map(m -> {
                    User u = userRepository.findById(m.getUserId()).orElseThrow();
                    return new TeamMemberResponse(u.getId(), u.getEmail(), u.getName(),
                            m.getRole(), m.getCreatedAt());
                }).toList();
    }
}
