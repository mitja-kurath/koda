package docs.koda.api.auth.controller;

import docs.koda.api.auth.dto.AuthResponse;
import docs.koda.api.auth.dto.LoginRequest;
import docs.koda.api.auth.dto.RefreshRequest;
import docs.koda.api.auth.dto.RegisterRequest;
import docs.koda.api.auth.service.AuthService;
import docs.koda.api.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request, HttpServletRequest http) {
        return authService.register(request, resolveIp(http));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request, HttpServletRequest http) {
        return authService.login(request, resolveIp(http));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody @Valid RefreshRequest request, HttpServletRequest http) {
        return authService.refresh(request.refreshToken(), resolveIp(http));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user, HttpServletRequest http) {
        authService.logout(user, resolveIp(http));
        return ResponseEntity.noContent().build();
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
