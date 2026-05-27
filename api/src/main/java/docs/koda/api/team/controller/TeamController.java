package docs.koda.api.team.controller;

import docs.koda.api.team.dto.InviteMemberRequest;
import docs.koda.api.team.dto.TeamMemberResponse;
import docs.koda.api.team.dto.TeamResponse;
import docs.koda.api.team.service.TeamService;
import docs.koda.api.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/me")
    public TeamResponse getMyTeam(@AuthenticationPrincipal User user) {
        return teamService.getMyTeam(user);
    }

    @GetMapping("/me/members")
    public List<TeamMemberResponse> getMembers(@AuthenticationPrincipal User user) {
        return teamService.getMembers(user);
    }

    @PostMapping("/me/members")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMemberResponse invite(@RequestBody @Valid InviteMemberRequest req,
                                      @AuthenticationPrincipal User user) {
        return teamService.invite(user, req.email());
    }

    @DeleteMapping("/me/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID userId,
                              @AuthenticationPrincipal User user) {
        teamService.removeMember(user, userId);
    }
}
