package docs.koda.api.auth.controller;

import docs.koda.api.auth.dto.LoginRequest;
import docs.koda.api.auth.dto.RegisterRequest;
import docs.koda.api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    public void login(@RequestBody @Valid LoginRequest request) {
        authService.login(request);
    }
}
