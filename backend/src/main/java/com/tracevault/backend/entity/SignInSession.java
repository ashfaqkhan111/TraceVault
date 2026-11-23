package com.tracevault.backend.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity 
@Table (name = "sign_in_session")
@NoArgsConstructor 
@Getter 
@Setter 
public class SignInSession {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column (name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @ManyToOne (fetch = FetchType.LAZY)
    @Column (name = "user_id", nullable = false)
    private User user;

    @Column (name = "device",length = 255)
    private String device;

    @Column (name = "browser", length = 255)
    private String browser;

    @Column (name = "ip_address")
    private String ipAddress;

    @Column (name = "signed_in_at")
    private OffsetDateTime signedInAt;

    @Column (name = "signed_out_at")
    private OffsetDateTime signedOutAt;


    @PrePersist 
    public void onCreate(){
        if(signedInAt == null){
            signedInAt = OffsetDateTime.now();
        }
    }
}
