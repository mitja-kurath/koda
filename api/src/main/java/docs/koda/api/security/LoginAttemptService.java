package docs.koda.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private record AttemptRecord(int count, Instant blockedUntil) {}

    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final long lockoutMinutes;

    public LoginAttemptService(
            @Value("${koda.rate-limit.max-attempts}") int maxAttempts,
            @Value("${koda.rate-limit.lockout-minutes}") long lockoutMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockoutMinutes = lockoutMinutes;
    }

    public boolean isBlocked(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null) return false;
        if (record.blockedUntil() != null && Instant.now().isBefore(record.blockedUntil())) {
            return true;
        }
        // Lockout expired — clear it
        if (record.blockedUntil() != null) {
            attempts.remove(key);
        }
        return false;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, existing) -> {
            int count = (existing == null ? 0 : existing.count()) + 1;
            Instant blockedUntil = count >= maxAttempts
                    ? Instant.now().plusSeconds(lockoutMinutes * 60)
                    : null;
            return new AttemptRecord(count, blockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }
}
