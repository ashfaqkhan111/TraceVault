package com.tracevault.backend.entity;

import java.util.UUID;

import com.tracevault.backend.enms.EmailLinkVerdict;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_link")
@Getter
@Setter
@NoArgsConstructor
public class EmailLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "link_id", nullable = false, updatable = false)
    private UUID linkId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_id", nullable = false)
    private EmailMessage emailMessage;

    @Column(name = "url", nullable = false, length = 4096)
    private String url;

    @Column(name = "resolved_domain", length = 255)
    private String resolvedDomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = false, length = 20)
    private EmailLinkVerdict verdict;
}
