package com.tracevault.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.UserSettings;
import com.tracevault.backend.repository.UserRepository;
import com.tracevault.backend.repository.UserSettingsRepository;

@Service
public class UserSettingService {
    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    public UserSettingService(UserSettingsRepository userSettingsRepository, UserRepository userRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserSettings getUserSettings(UUID userId) {
        return userSettingsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User settings not found."));
    }

    @Transactional
    public UserSettings createUserSettings(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        if (userSettingsRepository.existsById(userId)) {
            throw new IllegalArgumentException("User settings already exist.");
        }
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setPushEnabled(true);
        settings.setEmailEnabled(true);
        settings.setVerificationUpdates(true);
        return userSettingsRepository.save(settings);
    }

    @Transactional
    public UserSettings getOrCreateUserSettings(UUID userId) {
        return userSettingsRepository.findById(userId).orElseGet(() -> createUserSettings(userId));
    }

    @Transactional
    public UserSettings updateUserSettings(UUID userId, boolean pushEnabled,
                                           boolean emailEnabled, boolean verificationUpdates) {
        UserSettings settings = getOrCreateUserSettings(userId);
        settings.setPushEnabled(pushEnabled);
        settings.setEmailEnabled(emailEnabled);
        settings.setVerificationUpdates(verificationUpdates);
        return userSettingsRepository.save(settings);
    }
}
