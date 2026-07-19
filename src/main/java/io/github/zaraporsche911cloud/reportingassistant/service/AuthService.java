package io.github.zaraporsche911cloud.reportingassistant.service;

import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.dto.auth.AuthResponse;
import io.github.zaraporsche911cloud.reportingassistant.dto.auth.LoginRequest;
import io.github.zaraporsche911cloud.reportingassistant.dto.auth.RegisterRequest;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.UserRole;
import io.github.zaraporsche911cloud.reportingassistant.exception.ConflictException;
import io.github.zaraporsche911cloud.reportingassistant.exception.UnauthorizedException;
import io.github.zaraporsche911cloud.reportingassistant.repository.AppUserRepository;
import io.github.zaraporsche911cloud.reportingassistant.security.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokens;
    private final AuditService auditService;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenService tokens,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
        this.auditService = auditService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthResponse registerFirstAdministrator(RegisterRequest request) {
        if (userRepository.count() > 0) {
            throw new ConflictException("Initial registration is closed; ask an administrator to create your account");
        }
        AppUser user = userRepository.save(new AppUser(
                request.fullName(), request.email(), passwordEncoder.encode(request.password()), UserRole.ADMIN));
        auditService.record(user.getEmail(), "ADMIN_BOOTSTRAPPED", "USER", user.getId(), "First administrator created");
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        } catch (AuthenticationException exception) {
            auditService.record(request.email(), "LOGIN_FAILED", "AUTHENTICATION", null, "Invalid credentials");
            throw new UnauthorizedException("Invalid email or password");
        }
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        auditService.record(user.getEmail(), "LOGIN_SUCCEEDED", "AUTHENTICATION", user.getId(), null);
        return response(user);
    }

    public AuthResponse.UserSummary summary(AppUser user) {
        return new AuthResponse.UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.isEnabled());
    }

    private AuthResponse response(AppUser user) {
        JwtTokenService.IssuedToken token = tokens.issue(user);
        return new AuthResponse(token.value(), "Bearer", token.expiresAt(), summary(user));
    }
}
