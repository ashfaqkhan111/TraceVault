package com.tracevault.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Membuat dan memeriksa token JWT (format: header.payload.tanda-tangan).
 * Tanda tangan HMAC-SHA256 memastikan token tidak bisa dipalsukan tanpa tahu "secret".
 * Ditulis tanpa library tambahan supaya mudah dibaca.
 */
@Service
public class JwtService {

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();
    private static final String HEADER = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-hours:24}") long expirationHours) {
        if (secret.length() < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters long");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationHours * 3600;
    }

    public String createToken(UUID userId) {
        long now = Instant.now().getEpochSecond();
        String payloadJson = "{\"sub\":\"" + userId + "\",\"iat\":" + now + ",\"exp\":" + (now + expirationSeconds) + "}";
        String payload = B64.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String unsigned = HEADER + "." + payload;
        return unsigned + "." + B64.encodeToString(sign(unsigned));
    }

    /** @return id user jika token asli dan belum kedaluwarsa; kosong jika tidak valid. */
    public Optional<UUID> validate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !parts[0].equals(HEADER)) return Optional.empty();

            byte[] expected = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected, B64D.decode(parts[2]))) return Optional.empty();

            String json = new String(B64D.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher sub = Pattern.compile("\"sub\":\"([0-9a-fA-F-]{36})\"").matcher(json);
            Matcher exp = Pattern.compile("\"exp\":(\\d+)").matcher(json);
            if (!sub.find() || !exp.find()) return Optional.empty();
            if (Long.parseLong(exp.group(1)) < Instant.now().getEpochSecond()) return Optional.empty();

            return Optional.of(UUID.fromString(sub.group(1)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }
    }
}
