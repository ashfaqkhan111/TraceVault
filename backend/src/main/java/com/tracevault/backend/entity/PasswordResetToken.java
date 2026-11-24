package com.tracevault.backend.entity;

import java.util.UUID;
import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "password_reset_token")
@Getter 
@Setter 
@NoArgsConstructor 
public class PasswordResetToken {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column (name = "token_id", nullable = false, updatable = false)
    private UUID tokenId;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name="Token_hash",nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column (name = "used_at")
    private OffsetDateTime usedAt;



}
