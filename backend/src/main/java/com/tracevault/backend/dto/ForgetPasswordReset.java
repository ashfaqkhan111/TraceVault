package com.tracevault.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgetPasswordReset(
    @NotBlank (message = "Email is required.")
    @Email (message = "Enter a valid email.")
    String email
) {

}
