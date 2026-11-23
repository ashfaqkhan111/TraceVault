package com.tracevault.backend.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.UserSettings;
import com.tracevault.backend.service.UserSettingService;

@RestController
@RequestMapping("/api/users/{userId}/settings")
public class UserSettingsController {
    private final UserSettingService userSettingService;

    public UserSettingsController(UserSettingService userSettingService) {
        this.userSettingService = userSettingService;
    }

    @GetMapping
    public UserSettings getSettings(@PathVariable UUID userId) {
        return userSettingService.getOrCreateUserSettings(userId);
    }

    @PutMapping
    public UserSettings updateSettings(@PathVariable UUID userId, @RequestBody UserSettingsInput input) {
        return userSettingService.updateUserSettings(userId, input.pushEnabled(),
                input.emailEnabled(), input.verificationUpdates());
    }

    public record UserSettingsInput(boolean pushEnabled, boolean emailEnabled,
                                    boolean verificationUpdates) {}
}
