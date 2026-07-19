package io.github.zaraporsche911cloud.reportingassistant.controller;

import io.github.zaraporsche911cloud.reportingassistant.dto.auth.AuthResponse;
import io.github.zaraporsche911cloud.reportingassistant.dto.auth.LoginRequest;
import io.github.zaraporsche911cloud.reportingassistant.dto.auth.RegisterRequest;
import io.github.zaraporsche911cloud.reportingassistant.service.AuthService;
import io.github.zaraporsche911cloud.reportingassistant.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.created(URI.create("/api/v1/auth/me")).body(authService.registerFirstAdministrator(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse.UserSummary me() {
        return authService.summary(currentUserService.requireCurrentUser());
    }
}
