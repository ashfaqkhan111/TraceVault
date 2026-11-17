package com.tracevault.backend.repository;

import com.tracevault.backend.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserSettingsRepository  extends JpaRepository<UserSettings, UUID>{
    
}
