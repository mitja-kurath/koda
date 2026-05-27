package docs.koda.api.auth.service;

import docs.koda.api.auth.domain.RefreshToken;
import docs.koda.api.auth.repository.RefreshTokenRepository;
import docs.koda.api.security.JwtService;
import docs.koda.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtService jwtService;

    @Value("${koda.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Transactional
    public void store(User user, String rawToken) {
        repository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hash(rawToken))
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build());
    }

    @Transactional
    public User rotate(String rawToken, java.util.function.Function<String, User> userLookup) {
        String tokenHash = hash(rawToken);
        RefreshToken stored = repository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            repository.delete(stored);
            throw new BadCredentialsException("Refresh token expired");
        }

        String username = jwtService.extractUsername(rawToken);
        User user = userLookup.apply(username);

        // Rotation: delete old token, caller will store a new one
        repository.delete(stored);
        return user;
    }

    @Transactional
    public void revokeAll(User user) {
        repository.deleteByUserId(user.getId());
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
