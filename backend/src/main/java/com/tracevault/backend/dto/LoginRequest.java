package com.tracevault.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank (message = "Email riis required")
    @Email (message = "Enter a valid Email address.")
    String email,

    @NotBlank (message = "Password is required")
    String password

) {}
