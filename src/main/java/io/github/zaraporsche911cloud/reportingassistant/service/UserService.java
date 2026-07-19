package io.github.zaraporsche911cloud.reportingassistant.service;

import io.github.zaraporsche911cloud.reportingassistant.audit.AuditService;
import io.github.zaraporsche911cloud.reportingassistant.dto.user.UserDtos;
import io.github.zaraporsche911cloud.reportingassistant.entity.AppUser;
import io.github.zaraporsche911cloud.reportingassistant.entity.UserPreference;
import io.github.zaraporsche911cloud.reportingassistant.exception.ConflictException;
import io.github.zaraporsche911cloud.reportingassistant.exception.ResourceNotFoundException;
import io.github.zaraporsche911cloud.reportingassistant.repository.AppUserRepository;
import io.github.zaraporsche911cloud.reportingassistant.repository.UserPreferenceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final AppUserRepository users;
    private final UserPreferenceRepository preferences;
    private final PasswordEncoder encoder;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public UserService(
            AppUserRepository users,
            UserPreferenceRepository preferences,
            PasswordEncoder encoder,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.users = users;
        this.preferences = preferences;
        this.encoder = encoder;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public UserDtos.Response create(UserDtos.CreateRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) throw new ConflictException("User email is already in use");
        AppUser actor = currentUserService.requireCurrentUser();
        AppUser user = users.save(new AppUser(request.fullName(), request.email(), encoder.encode(request.password()), request.role()));
        auditService.record(actor.getEmail(), "USER_CREATED", "USER", user.getId(), "Role: " + user.getRole());
        return response(user);
    }

    public List<UserDtos.Response> list() {
        return users.findAll(Sort.by("fullName")).stream().map(this::response).toList();
    }

    @Transactional
    public UserDtos.Response update(Long id, UserDtos.UpdateRequest request) {
        AppUser actor = currentUserService.requireCurrentUser();
        AppUser user = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (actor.getId().equals(id) && !request.enabled()) {
            throw new ConflictException("An administrator cannot disable their own account");
        }
        user.changeRole(request.role());
        user.setEnabled(request.enabled());
        auditService.record(actor.getEmail(), "USER_UPDATED", "USER", id, "Role: " + request.role() + ", enabled: " + request.enabled());
        return response(user);
    }

    @Transactional
    public UserDtos.Response updateProfile(UserDtos.ProfileRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        user.updateProfile(request.fullName());
        auditService.record(user.getEmail(), "PROFILE_UPDATED", "USER", user.getId(), null);
        return response(user);
    }

    public UserDtos.Response currentProfile() {
        return response(currentUserService.requireCurrentUser());
    }

    public UserDtos.PreferenceResponse preferences() {
        AppUser user = currentUserService.requireCurrentUser();
        return preferences.findByUserId(user.getId())
                .map(value -> new UserDtos.PreferenceResponse(value.isDarkMode(), value.getTimezone()))
                .orElse(new UserDtos.PreferenceResponse(false, "UTC"));
    }

    @Transactional
    public UserDtos.PreferenceResponse updatePreferences(UserDtos.PreferenceRequest request) {
        AppUser user = currentUserService.requireCurrentUser();
        UserPreference preference = preferences.findByUserId(user.getId()).orElseGet(() -> new UserPreference(user));
        preference.update(request.darkMode(), request.timezone());
        preferences.save(preference);
        return new UserDtos.PreferenceResponse(preference.isDarkMode(), preference.getTimezone());
    }

    private UserDtos.Response response(AppUser user) {
        return new UserDtos.Response(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}
