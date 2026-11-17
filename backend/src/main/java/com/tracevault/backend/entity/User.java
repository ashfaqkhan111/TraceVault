package com.tracevault.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tracevault.backend.enms.AccountStatus;
import com.tracevault.backend.enms.AuthProvider;

@Entity 
@Table (name = "users")
@Getter 
@Setter 
@NoArgsConstructor 
public class User {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column (name = "user_id",nullable = false, updatable = false)
    private UUID userId;

    @Column (name = "account_code", length = 255, unique = true, nullable = false)
    private String accountCode;

    @Column (name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column (name = "email", length = 255, unique = true, nullable = false)
    private String email;

    @Column (name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated (EnumType.STRING)
    @Column (name = "auth_provider",length = 20, nullable = false)
    private AuthProvider authProvider;

    @Enumerated (EnumType.STRING)
    @Column (name = "account_status", length = 20, nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column (name = "terms_accepted_at")
    private OffsetDateTime termsAcceptedAt;

    @Column (name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column (name = "last_sign_in_at")
    private OffsetDateTime lastSignInAt;

    @Column (name = "delete_at")
    private OffsetDateTime deleteAt;


    @PrePersist 
    protected void onCreate(){
        if(createdAt == null){
            createdAt = OffsetDateTime.now();
        }
    }
    
}
