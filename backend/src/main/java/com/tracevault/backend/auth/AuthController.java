package com.tracevault.backend.auth;

import com.tracevault.backend.auth.AuthDtos.AuthResponse;
import com.tracevault.backend.auth.AuthDtos.LoginRequest;
import com.tracevault.backend.auth.AuthDtos.MessageResponse;
import com.tracevault.backend.auth.AuthDtos.PasswordResetRequest;
import com.tracevault.backend.auth.AuthDtos.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/register        → buat akun baru
 * POST /api/auth/login           → masuk, dapat token
 * POST /api/auth/password-reset  → minta reset password
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /**
     * Belum mengirim email sungguhan (butuh layanan SMTP).
     * Jawabannya selalu sama, supaya orang tidak bisa mengecek email mana yang terdaftar.
     */
    @PostMapping("/password-reset")
    public MessageResponse requestPasswordReset(@Valid @RequestBody PasswordResetRequest req) {
        return new MessageResponse("If an account exists for that email, a reset link has been sent.");
    }
}
