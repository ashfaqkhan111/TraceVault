package com.tracevault.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.tracevault.backend.dto.RegisterRequest;
import com.tracevault.backend.dto.UserResponseDTO;
import com.tracevault.backend.service.AuthService;

@RestController 
@RequestMapping ("/api/auth")
@Validated 
public class AuthController {

    private final AuthService authService;

    public  AuthController(AuthService authService){
        this.authService = authService;

    }

    @PostMapping ("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequest request){
        UserResponseDTO response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
}
