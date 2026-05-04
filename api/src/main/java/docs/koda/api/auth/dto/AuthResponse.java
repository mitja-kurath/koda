package docs.koda.api.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    public String accessToken;
    public String refreshToken;
}
