package com.tracevault.backend.auth;

import com.tracevault.backend.auth.AuthDtos.AuthResponse;
import com.tracevault.backend.auth.AuthDtos.LoginRequest;
import com.tracevault.backend.auth.AuthDtos.RegisterRequest;
import com.tracevault.backend.auth.AuthDtos.UserView;
import com.tracevault.backend.config.JwtService;
import com.tracevault.backend.user.User;
import com.tracevault.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("forensic_investigator", "security_analyst", "incident_responder", "law_enforcement",
                    "researcher", "student", "other"); // sama dengan pilihan di pages/signup.html

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        String[] parts = req.fullName().trim().split("\\s+", 2);
        String role = req.role() != null && ALLOWED_ROLES.contains(req.role()) ? req.role() : "forensic_investigator";

        User user = users.save(new User(
                parts[0],
                parts.length > 1 ? parts[1] : "",
                email,
                passwordEncoder.encode(req.password()),
                role));
        return new AuthResponse(jwtService.createToken(user.getId()), UserView.of(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                // Pesan dibuat sama untuk email salah & password salah, supaya orang tidak bisa menebak email terdaftar.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        return new AuthResponse(jwtService.createToken(user.getId()), UserView.of(user));
    }
}
