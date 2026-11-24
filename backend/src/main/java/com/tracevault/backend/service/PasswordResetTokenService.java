package com.tracevault.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tracevault.backend.entity.User;
import com.tracevault.backend.entity.PasswordResetToken;
import com.tracevault.backend.repository.UserRepository;
import com.tracevault.backend.repository.PasswordResetTokenRepository;

@Service 
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetTokenService(PasswordResetTokenRepository tokenRepository, UserRepository userRepository,
                                     PasswordEncoder passwordEncoder){
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

   @Transactional 
   public String createResetToken(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);

    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    
    String tokenHash = hashToken(rawToken);

    PasswordResetToken resetToken = new PasswordResetToken();

    resetToken.setUser(user);
    resetToken.setTokenHash(tokenHash);
    resetToken.setExpiresAt(OffsetDateTime.now().plusMinutes(15));

    tokenRepository.save(resetToken);

    return rawToken;
   }

   public PasswordResetToken validateResetToken(String rawToken){
    String tokenHash = hashToken(rawToken);

    PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash).orElseThrow(() -> new RuntimeException("Invalid reset token") );

    if(resetToken.getUsedAt() != null){
        throw new RuntimeException("Token has already been used");
    }

    if(resetToken.getExpiresAt().isBefore(OffsetDateTime.now())){
        throw new RuntimeException("Token has expired.");
    }
    return resetToken;
   }

public  String hashToken(String rawToken){
    try{
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hashByte = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashByte);
    }catch (Exception e){
        throw new RuntimeException("Unable to hash token ",e);
    }
}

@Transactional
public void resetPassword(String rawToken, String newPassword){
    if (newPassword == null || newPassword.length() < 8) {
        throw new IllegalArgumentException("New password must be at least 8 characters long.");
    }
    PasswordResetToken resetToken = validateResetToken(rawToken);
    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    resetToken.setUsedAt(OffsetDateTime.now());
    tokenRepository.save(resetToken);
}

@Transactional
public String createResetTokenForEmail(String email) {

    String normalizedEmail = email.trim()
            .toLowerCase(Locale.ROOT);

    Optional<User> optionalUser =
            userRepository.findByEmailIgnoreCase(normalizedEmail);
  
    if (optionalUser.isEmpty()) {
        return null;
    }

    User user = optionalUser.get();
    return createResetToken(user.getUserId());
}

    
}
