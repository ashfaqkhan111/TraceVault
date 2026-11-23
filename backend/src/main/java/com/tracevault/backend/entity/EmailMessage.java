package com.tracevault.backend.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tracevault.backend.enms.EmailSignatureResult;
import com.tracevault.backend.enms.SpfResult;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_message")
@Getter
@Setter
@NoArgsConstructor
public class EmailMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "email_id", nullable = false, updatable = false)
    private UUID emailId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_id", nullable = false, unique = true)
    private Evidence evidence;

    @Column(name = "from_display_name", length = 255)
    private String fromDisplayName;

    @Column(name = "from_address", length = 320)
    private String fromAddress;

    @Column(name = "reply_to", length = 320)
    private String replyTo;

    @Column(name = "subject", length = 998)
    private String subject;

    /** Ordered Received headers, serialized as a JSON array. */
    @Column(name = "received_hops", columnDefinition = "JSON")
    private String receivedHops;

    @Enumerated(EnumType.STRING)
    @Column(name = "spf_result", nullable = false, length = 10)
    private SpfResult spfResult = SpfResult.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "dkim_result", nullable = false, length = 10)
    private EmailSignatureResult dkimResult = EmailSignatureResult.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "dmarc_result", nullable = false, length = 10)
    private EmailSignatureResult dmarcResult = EmailSignatureResult.NONE;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;
}
