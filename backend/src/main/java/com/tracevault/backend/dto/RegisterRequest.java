package com.tracevault.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 255)
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    String password,

    @NotBlank(message = "Please confirm your password")
    String confirmPassword,

    @AssertTrue(message = "You must accept the terms")
    boolean termsAccepted

) {

    @AssertTrue(message = "Password and confirm password do not match")
    public boolean isPasswordMatching() {
        return password != null
                && password.equals(confirmPassword);
    }
}