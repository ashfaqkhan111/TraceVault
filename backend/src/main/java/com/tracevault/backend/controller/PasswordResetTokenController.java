package com.tracevault.backend.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tracevault.backend.service.PasswordResetTokenService;

@RestController 
@RequestMapping ("/api/password-reset")
public class PasswordResetTokenController {

    private final PasswordResetTokenService tokenService;

    public PasswordResetTokenController(PasswordResetTokenService tokenService){
        this.tokenService = tokenService;
    }

    @RequestMapping ("/request/{userId}")
    public ResponseEntity<String> createResetToken(@PathVariable  UUID userId){
        String token = tokenService.createResetToken(userId);

        return ResponseEntity.ok("Passowrd reset token generated successfully.");
    }

    @GetMapping ("/validate")
    public ResponseEntity<String> validateResetToken(@RequestParam String token){
        tokenService.validateResetToken(token);

        return ResponseEntity.ok("Password reset token is valid");
    }

    @PostMapping ("/use")
    public  ResponseEntity<String> resetPassword(@RequestParam String token,
                                                 @RequestParam String newPassword){
        tokenService.resetPassword(token, newPassword);
        return  ResponseEntity.ok("Password has been reset successfully.");
    }
    
}
