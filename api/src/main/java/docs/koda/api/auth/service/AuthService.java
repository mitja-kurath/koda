package docs.koda.api.auth.service;

import docs.koda.api.auth.dto.AuthResponse;
import docs.koda.api.auth.dto.LoginRequest;
import docs.koda.api.auth.dto.RegisterRequest;
import docs.koda.api.security.JwtService;
import docs.koda.api.user.domain.Team;
import docs.koda.api.user.domain.User;
import docs.koda.api.user.repository.TeamRepository;
import docs.koda.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .email(request.email)
                .name(request.name)
                .password(passwordEncoder.encode(request.password))
                .build();
        userRepository.save(user);

        teamRepository.save(Team.builder()
                .name(request.name + "'s Team")
                .ownerId(user.getId())
                .plan(Team.Plan.FREE)
                .build());

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email, request.password));
        User user = userRepository.findByEmail(request.email).orElseThrow();
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}
