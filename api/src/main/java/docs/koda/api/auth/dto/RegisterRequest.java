package docs.koda.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @Email
    public String email;

    @Size(min = 8)
    public String password;
}
