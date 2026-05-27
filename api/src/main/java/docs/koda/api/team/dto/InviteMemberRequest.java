package docs.koda.api.team.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteMemberRequest(@Email @NotBlank String email) {}
