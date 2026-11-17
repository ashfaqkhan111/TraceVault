package com.tracevault.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "user_settings")
@Getter 
@Setter 
@NoArgsConstructor 
public class UserSettings {

    @Id 
    @Column (name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId 
    @JoinColumn (name = "user_id")
    private User user;

    @Column (name = "push_enabled", nullable = false)
    private boolean pushEnabled = true;

    @Column (name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column (name = "verification_updates", nullable = false)
    private boolean verificationUpdates = true;
    

}
