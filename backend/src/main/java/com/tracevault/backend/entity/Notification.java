package com.tracevault.backend.entity;

import java.util.UUID;

import com.tracevault.backend.enms.NotificationCategory;
import com.tracevault.backend.enms.NotificationTargetType;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity 
@Table (name = "notification")
@Getter 
@Setter 
@NoArgsConstructor 
public class Notification {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column (name = "notification_id", nullable = false, updatable = false)
    private UUID notificationId;

    @ManyToOne (fetch = FetchType.LAZY)
    @Column (name = "user_id", nullable = false)
    private User user;

    @Column (name = "title", nullable =  false, length = 255)
    private String title;

    @Enumerated (EnumType.STRING)
    @Column (name = "category", nullable = false)
    private NotificationCategory category;

    @Column (name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column (name = "action_label")
    private String actionLabel;

    @Enumerated (EnumType.STRING)
    @Column (name = "target_type", nullable = false)
    private NotificationTargetType targetType;

    @Column (name = "target_id")
    private UUID targetId;

    @Column (name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column (name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist 
    protected void onCreate(){
        if(createdAt == null){
            createdAt = OffsetDateTime.now();
        }
    }
    
}
