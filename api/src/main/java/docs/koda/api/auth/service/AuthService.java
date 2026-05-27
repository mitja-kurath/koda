package docs.koda.api.auth.service;

import docs.koda.api.audit.domain.AuditLog;
import docs.koda.api.audit.service.AuditLogService;
import docs.koda.api.auth.dto.AuthResponse;
import docs.koda.api.auth.dto.LoginRequest;
import docs.koda.api.auth.dto.RegisterRequest;
import docs.koda.api.security.JwtService;
import docs.koda.api.security.LoginAttemptService;
import docs.koda.api.team.domain.TeamMember;
import docs.koda.api.team.repository.TeamMemberRepository;
import docs.koda.api.user.domain.Team;
import docs.koda.api.user.domain.User;
import docs.koda.api.user.repository.TeamRepository;
import docs.koda.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .email(request.email)
                .name(request.name)
                .password(passwordEncoder.encode(request.password))
                .build();
        userRepository.save(user);

        Team team = Team.builder()
                .name(request.name + "'s Team")
                .ownerId(user.getId())
                .plan(Team.Plan.FREE)
                .build();
        teamRepository.save(team);

        teamMemberRepository.save(TeamMember.builder()
                .teamId(team.getId())
                .userId(user.getId())
                .role(TeamMember.Role.OWNER)
                .build());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.store(user, refreshToken);

        auditLogService.log(AuditLog.Event.REGISTER_SUCCESS, user.getId(), ipAddress, user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(LoginRequest request, String ipAddress) {
        String key = request.email + "|" + ipAddress;

        if (loginAttemptService.isBlocked(key)) {
            auditLogService.log(AuditLog.Event.LOGIN_BLOCKED, null, ipAddress, request.email);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed attempts. Try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email, request.password));
        } catch (BadCredentialsException ex) {
            loginAttemptService.recordFailure(key);
            auditLogService.log(AuditLog.Event.LOGIN_FAILURE, null, ipAddress, request.email);
            throw ex;
        }

        User user = userRepository.findByEmail(request.email).orElseThrow();
        loginAttemptService.recordSuccess(key);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.store(user, refreshToken);

        auditLogService.log(AuditLog.Event.LOGIN_SUCCESS, user.getId(), ipAddress, user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken, String ipAddress) {
        User user = refreshTokenService.rotate(rawRefreshToken,
                email -> userRepository.findByEmail(email).orElseThrow(
                        () -> new BadCredentialsException("User not found")));

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.store(user, newRefreshToken);

        auditLogService.log(AuditLog.Event.TOKEN_REFRESH, user.getId(), ipAddress, null);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(User user, String ipAddress) {
        refreshTokenService.revokeAll(user);
        auditLogService.log(AuditLog.Event.LOGOUT, user.getId(), ipAddress, null);
    }
}
