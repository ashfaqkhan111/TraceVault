package com.tracevault.backend.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID>{

    List<Notification> findByUser_UserId(UUID userId);

    List<Notification> findByUser_UserIdAndIsReadFalse(UUID userId);

    List<Notification> findByUser_UserIdAndIsReadTrue(UUID userId);

    long countByUser_UserIdAndIsReadFalse(UUID userId);
}
