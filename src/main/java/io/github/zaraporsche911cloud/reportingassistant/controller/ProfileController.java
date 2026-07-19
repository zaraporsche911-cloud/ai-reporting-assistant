package io.github.zaraporsche911cloud.reportingassistant.controller;

import io.github.zaraporsche911cloud.reportingassistant.dto.user.UserDtos;
import io.github.zaraporsche911cloud.reportingassistant.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) { this.userService = userService; }

    @GetMapping
    public UserDtos.Response profile() { return userService.currentProfile(); }

    @PutMapping
    public UserDtos.Response update(@Valid @RequestBody UserDtos.ProfileRequest request) {
        return userService.updateProfile(request);
    }

    @GetMapping("/preferences")
    public UserDtos.PreferenceResponse preferences() { return userService.preferences(); }

    @PutMapping("/preferences")
    public UserDtos.PreferenceResponse preferences(@Valid @RequestBody UserDtos.PreferenceRequest request) {
        return userService.updatePreferences(request);
    }
}
