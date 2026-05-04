package docs.koda.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @Email
    @NotBlank
    public String email;

    @Size(min = 8)
    @NotBlank
    public String password;

    @Size(min = 2)
    @NotBlank
    public String name;
}
