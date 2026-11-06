package com.tracevault.backend.auth;

import com.tracevault.backend.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Bentuk data (DTO) yang dikirim/diterima endpoint /api/auth.
 * Nama field HARUS sama dengan yang dipakai frontend/js/api.js dan auth.js.
 */
public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") String email,
            @NotBlank(message = "Password is required.") String password) {}

    public record RegisterRequest(
            @NotBlank(message = "Full name is required.") @Size(max = 200) String fullName,
            @NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") String email,
            @NotBlank(message = "Password is required.")
            @Size(min = 8, max = 100, message = "Password must be at least 8 characters.") String password,
            String role) {}

    public record PasswordResetRequest(
            @NotBlank(message = "Email is required.") @Email(message = "Email format is invalid.") String email) {}

    public record UserView(String id, String firstName, String lastName, String email, String role) {
        public static UserView of(User u) {
            return new UserView(u.getId().toString(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getRole());
        }
    }

    /** Frontend menyimpan token di localStorage("tv_token") dan user di localStorage("tv_user"). */
    public record AuthResponse(String token, UserView user) {}

    public record MessageResponse(String message) {}
}
